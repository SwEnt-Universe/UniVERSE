package com.android.universe.utils

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * An object to manage the connection to Firebase Emulators for Android tests.
 *
 * This object will automatically use the emulators if they are running when the tests start.
 */
object FirebaseEmulator {
  @Volatile private var isConnected = false
  private val lock = Any()

  // 127.0.0.1 (localhost) is used for Robolectric tests running on the JVM
  private const val ROBOLECTRIC_HOST = "127.0.0.1"
  // 10.0.2.2 is for tests running on an Android Emulator
  private const val ANDROID_EMULATOR_HOST = "10.0.2.2"

  const val AUTH_PORT = 9099
  const val FIRESTORE_PORT = 8080

  val auth: FirebaseAuth
    get() = Firebase.auth

  val firestore: FirebaseFirestore
    get() = Firebase.firestore

  /**
   * Connects the Firebase instance to the local emulators. This MUST be called after
   * FirebaseApp.initializeApp() has run.
   *
   * @param isRobolectric Set to true when running on Robolectric (uses localhost), false when
   *   running on an Android Emulator (uses 10.0.2.2).
   */
  fun connect(isRobolectric: Boolean) {
    if (isConnected) return

    synchronized(lock) {
      if (isConnected) return

      val host = if (isRobolectric) ROBOLECTRIC_HOST else ANDROID_EMULATOR_HOST

      auth.useEmulator(host, AUTH_PORT)
      firestore.useEmulator(host, FIRESTORE_PORT)
      isConnected = true
    }
  }
}
