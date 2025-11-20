package com.android.universe.model.user

import android.util.Log
import com.android.universe.di.DefaultDP
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

/**
 * A reactive, read-optimized facade over Firestore **user** documents.
 *
 * ## What this class does
 * - Exposes **hot** Kotlin Flows of `UserProfile` per `uid` using a Firestore **snapshot listener**
 *   (real-time subscription).
 * - **Caches** one shared upstream listener per user id to avoid redundant network connections and
 *   reads when the same user is needed in multiple places (lists, details, etc.).
 * - Emits updates **automatically** whenever the user document changes in Firestore (e.g., user
 *   edits first/last name in settings).
 *
 * ## Why this reduces database reads
 * Traditional calls like `document(uid).get()` are one-shot: every refresh is another read. A
 * snapshot listener, on the other hand, performs:
 * - **1 initial read** when the listener attaches, then
 * - **1 incremental read per actual document change**, and
 * - **0 reads while the document is unchanged** (even across many consumers).
 *
 * This repository shares that single listener across all collectors for a given `uid` by using
 * `shareIn(…, started = Eagerly, replay = 1)`. The first consumer starts the upstream; subsequent
 * consumers reuse it and receive the latest value instantly from the replay cache (no extra
 * Firestore reads).
 *
 * ## How it works internally
 * - Wraps `addSnapshotListener` in a `callbackFlow` to bridge Firestore callbacks → Flow.
 * - Converts snapshots to `UserProfile` and emits through the Flow.
 * - Shares each per-uid Flow **eagerly** in a dedicated `CoroutineScope`, so the Firestore listener
 *   stays active while the repository lives (or until the scope is canceled).
 *
 * ## Lifecycle & threading
 * - The repository owns a `CoroutineScope(scopeDispatcher + SupervisorJob())`. Pass a test
 *   dispatcher in tests.
 * - Callers **do not** manage listener lifecycles; the repository does. Cancel the repository scope
 *   (e.g., by making it app-scoped and letting the process kill it) to tear down listeners.
 *
 * ## Offline behavior
 * Firestore’s local cache serves the latest known data when offline; updates resume automatically
 * when connectivity returns. Listeners deliver cached data immediately, improving perceived
 * latency.
 *
 * ## Error handling
 * - Firestore errors close the channel; collectors will see completion.
 * - Snapshot conversion errors (e.g., malformed `dateOfBirth`) are **logged and ignored** to avoid
 *   spurious emissions with bad data. The listener remains active and will emit on the next good
 *   snapshot.
 *
 * ## When to use
 * - Any UI that should stay in sync with a user’s profile (creator names, participants, etc.)
 *   **without** manual refresh or polling.
 * - Lists that render many events from the same creators—this cache avoids “N listeners per card”.
 *
 * ## Example (ViewModel)
 *
 * ```kotlin
 * class EventVM(
 *   private val userRx: UserReactiveRepository
 * ) : ViewModel() {
 *   fun creatorNameFlow(creatorUid: String): Flow<String> =
 *     userRx.getUserFlow(creatorUid).map { u ->
 *       u?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"
 *     }
 * }
 * ```
 *
 * ## Example (Compose)
 *
 * ```kotlin
 * @Composable
 * fun CreatorName(creatorUid: String, vm: EventVM) {
 *   val name by vm.creatorNameFlow(creatorUid)
 *     .collectAsState(initial = "Loading…")
 *   Text(name)
 * }
 * ```
 *
 * @param db Firestore instance.
 * @param scopeDispatcher Dispatcher used for the internal sharing scope. Defaults to IO.
 */
class UserReactiveRepository(
    private val db: FirebaseFirestore,
    scopeDispatcher: CoroutineDispatcher = DefaultDP.io
) {
  // Cache of user flows so we don't re-subscribe for the same uid
  private val userFlows = mutableMapOf<String, SharedFlow<UserProfile?>>()
  private val scope = CoroutineScope(scopeDispatcher + SupervisorJob())

  /**
   * Closes this repository and cancels all active snapshot listeners.
   *
   * This should be called when the repository is no longer needed (e.g. app shutdown, test
   * teardown, or ViewModel clear if used locally).
   */
  fun close() {
    Log.i("UserReactiveRepository", "Closing repository and cancelling listeners")
    scope.cancel() // triggers awaitClose { listener.remove() }
    userFlows.clear()
  }

  /**
   * Returns a **hot, shared** `Flow<UserProfile?>` that emits the user profile for the given [uid]
   * and continues to emit whenever the Firestore document changes.
   *
   * ### Semantics
   * - **Hot shared stream**: multiple callers receive the **same** upstream listener and the
   *   **latest** value immediately (thanks to `replay = 1`), without creating additional Firestore
   *   reads.
   * - **Emission contract**:
   *     - Emits once initially with the current document (1 read).
   *     - Emits again on each actual document change (1 read per change).
   *     - Does **not** emit for identical/no-op server updates.
   * - **Null handling**:
   *     - If the snapshot is unexpectedly null or cannot be converted (e.g., bad data), the error
   *       is logged and **no value is emitted** for that update.
   * - **Completion**:
   *     - The flow remains active as long as the repository’s internal scope is alive. Firestore
   *       errors will close the flow; you can resubscribe to recreate it.
   *
   * ### Why this lowers cost
   * Without this method, each UI site might call `document(uid).get()` repeatedly, producing many
   * reads. This method establishes a single listener for [uid], which:
   * - serves all collectors,
   * - only incurs reads **on change**, and
   * - provides instantaneous updates across the app.
   *
   * ### Usage
   *
   * ```kotlin
   * // ViewModel: combine events with creator profiles
   * val eventsWithCreators: Flow<List<Pair<Event, UserProfile?>>> =
   *   eventsFlow.flatMapLatest { events ->
   *     val ids = events.map { it.creator }.distinct()
   *     combine(ids.map { id -> getUserFlow(id).map { id to it } }) { pairs ->
   *       val byId = pairs.toMap()
   *       events.map { e -> e to byId[e.creator] }
   *     }
   *   }
   * ```
   *
   * ### Notes & pitfalls
   * - This method caches the created shared flow in-memory. If you expect thousands of distinct
   *   user IDs simultaneously, consider adding an eviction policy (e.g., LRU).
   * - The internal `userFlows` map is not synchronized; if you access from multiple threads, either
   *   confine calls to a single thread (typical in app code) or switch to a thread-safe map (e.g.,
   *   `ConcurrentHashMap`) or guard with a `Mutex`.
   *
   * @param uid The Firestore document id under `users/{uid}`.
   * @return A **shared** Flow that emits `UserProfile?` on initial load and on subsequent updates.
   */
  fun getUserFlow(uid: String): Flow<UserProfile?> {
    return userFlows.getOrPut(uid) {
      callbackFlow {
            val listener =
                db.collection(USERS_COLLECTION_PATH).document(uid).addSnapshotListener {
                    snapshot,
                    error ->
                  if (error != null) {
                    close(error)
                    return@addSnapshotListener
                  }
                  /**
                   * The function [getUserFlow] is called without reason sometimes as such bad data
                   * can be received and notably the date of birth will throw an exception which
                   * needs to be caught but not resent
                   */
                  try {
                    val user = documentToUserProfile(snapshot!!)
                    trySend(user)
                  } catch (e: NullPointerException) {
                    Log.e(
                        "UserReactiveRepository",
                        "Error converting document to UserProfile bad data received",
                        e)
                  }
                }
            awaitClose { listener.remove() }
          }
          // shareIn makes sure all collectors share the same Firestore listener
          .shareIn(scope = scope, started = SharingStarted.Eagerly, replay = 1)
    }
  }
}
