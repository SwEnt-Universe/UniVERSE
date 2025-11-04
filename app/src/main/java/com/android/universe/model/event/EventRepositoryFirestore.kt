package com.android.universe.model.event

import android.util.Log
import com.android.universe.model.Tag
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.tasks.await

// Firestore collection path for events.
const val EVENTS_COLLECTION_PATH = "events"

/** Check if a List is of type T and safely casts it, returning an empty list if not. */
private inline fun <reified T> Any?.safeCastList(): List<T> {
  return if (this is List<*>) {
    this.filterIsInstance<T>()
  } else emptyList()
}

/** Check if a Map is of type K to V and safely casts it, returning an empty map if not. */
private inline fun <reified K, reified V> Any?.safeCastMap(): Map<K, V> {
  return if (this is Map<*, *>) {
    this.filterKeys { it is K }
        .mapKeys { it.key as K }
        .filterValues { it is V }
        .mapValues { it.value as V }
  } else emptyMap()
}

/**
 * Firestore implementation of [EventRepository] to stock events in the firestore database.
 *
 * Stores events in the database and persist data between app launches.
 */
class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {
  /**
   * Converts a UserProfile object to a Map<String, Any?>.
   *
   * @param user the UserProfile to convert.
   * @return a map representation of the UserProfile.
   */
  private fun userProfileToMap(user: UserProfile): Map<String, Any?> {
    return mapOf(
        "uid" to user.uid,
        "username" to user.username,
        "firstName" to user.firstName,
        "lastName" to user.lastName,
        "country" to user.country,
        "description" to user.description,
        "dateOfBirth" to user.dateOfBirth.toString(),
        "tags" to user.tags.map { it.ordinal })
  }

  /**
   * Converts a Map<String, Any?> to a UserProfile object.
   *
   * @param map the map to convert.
   * @return the corresponding UserProfile object.
   */
  private fun mapToUserProfile(map: Map<String, Any?>): UserProfile {
    return UserProfile(
        uid = map["uid"] as String,
        username = map["username"] as String,
        firstName = map["firstName"] as String,
        lastName = map["lastName"] as String,
        country = map["country"] as String,
        description = map["description"] as String?,
        dateOfBirth = LocalDate.parse(map["dateOfBirth"] as String),
        tags =
            (map["tags"].safeCastList<Number>())
                .map { ordinal -> Tag.entries[ordinal.toInt()] }
                .toSet())
  }

  /**
   * Converts a Location object to a Map<String, Any?>.
   *
   * @param location the location to convert.
   * @return a map representation of the Location.
   */
  private fun locationToMap(location: Location): Map<String, Any?> {
    return mapOf("latitude" to location.latitude, "longitude" to location.longitude)
  }

  /**
   * Converts a Map<String, Any?> to a Location object.
   *
   * @param map the map to convert.
   * @return the corresponding location object.
   */
  private fun mapToLocation(map: Map<String, Any?>): Location {
    val latitude = map["latitude"] as? Double ?: 0.0
    val longitude = map["longitude"] as? Double ?: 0.0
    return Location(latitude, longitude)
  }

  /**
   * Converts an Event object to a Map<String, Any?>.
   *
   * @param event the Event to convert.
   * @return a map representation of the Event.
   */
  private fun eventToMap(event: Event): Map<String, Any?> {
    return mapOf(
        "id" to event.id,
        "title" to event.title,
        "description" to event.description,
        "date" to event.date.toString(),
        "tags" to event.tags.map { it.ordinal },
        "participants" to event.participants.map { it -> userProfileToMap(it) },
        "creator" to userProfileToMap(event.creator),
        "location" to locationToMap(event.location))
  }

  /**
   * Converts a Firestore [DocumentSnapshot] to an [Event] object.
   *
   * @param doc the DocumentSnapshot to convert.
   * @return the corresponding Event object.
   */
  private fun documentToEvent(doc: DocumentSnapshot): Event {
    return try {
      // the list of tags that have been casted safely.
      val tagsList = doc.get("tags").safeCastList<Number>()
      // the creator map that have been casted safely.
      val creatorMap = doc.get("creator").safeCastMap<String, Any?>()
      // check if the creator map is empty and throw an exception if so.
      require(creatorMap.isNotEmpty()) { "Creator data missing" }
      // the participants list that have been casted safely.
      val participantsList = doc.get("participants").safeCastList<Map<String, Any?>>()
      // the location map that have been casted safely.
      val locationMap = doc.get("location").safeCastMap<String, Any?>()
      // check if the location map is empty and throw an exception if so.
      require(locationMap.isNotEmpty()) { "Location data missing" }
      Event(
          id = doc.getString("id") ?: "",
          title = doc.getString("title") ?: "",
          description = doc.getString("description"),
          date = doc.getString("date")?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
          tags = tagsList.map { ordinal -> Tag.entries[ordinal.toInt()] }.toSet(),
          creator = mapToUserProfile(creatorMap),
          participants = participantsList.map { mapToUserProfile(it) }.toSet(),
          location = mapToLocation(locationMap))
    } catch (e: Exception) {
      Log.e("EventRepositoryFirestore", "Error converting document to Event", e)
      throw e
    }
  }

  /**
   * Retrieves all events currently stored in the repository.
   *
   * @return a list of [Event] objects.
   */
  override suspend fun getAllEvents(): List<Event> {
    val events = ArrayList<Event>()
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()

    for (document in querySnapshot.documents) {
      val event = documentToEvent(document)
      events.add(event)
    }
    return events
  }

  /**
   * Retrieves an event by its ID.
   *
   * @param eventId the unique ID of the event.
   * @return the [Event] associated with the given ID.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun getEvent(eventId: String): Event {
    val event = db.collection(EVENTS_COLLECTION_PATH).document(eventId).get().await()
    if (event.exists()) {
      return documentToEvent(event)
    } else {
      throw NoSuchElementException("No event with ID $eventId found")
    }
  }

  /**
   * Retrieves suggested events for a given user based on their profile (tags).
   *
   * @param user the [UserProfile] for whom to suggest events.
   * @return a list of suggested [Event] objects.
   */
  override suspend fun getSuggestedEventsForUser(user: UserProfile): List<Event> {
    val matchedEvents = getEventsMatchingUserTags(user)
    val rankedEvents = rankEventsByTagMatch(user, matchedEvents)
    return rankedEvents.map { it.first }
  }

  /**
   * Adds a new event to the repository.
   *
   * @param event the [Event] to add.
   */
  override suspend fun addEvent(event: Event) {
    db.collection(EVENTS_COLLECTION_PATH).document(event.id).set(eventToMap(event)).await()
  }

  /**
   * Updates an existing event identified by eventId.
   *
   * @param eventId the ID of the event to update.
   * @param newEvent the new [Event] to replace the old one.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun updateEvent(eventId: String, newEvent: Event) {
    val event = db.collection(EVENTS_COLLECTION_PATH).document(eventId).get().await()
    if (event.exists()) {
      db.collection(EVENTS_COLLECTION_PATH)
          .document(eventId)
          .set(eventToMap(newEvent.copy(id = eventId)))
          .await()
    } else {
      throw NoSuchElementException("No event with ID $eventId found")
    }
  }

  private suspend fun getEventsMatchingUserTags(user: UserProfile): List<Event> {
    val userTagOrdinals = user.tags.map { it.ordinal }
    if (userTagOrdinals.isEmpty()) return emptyList()

    val querySnapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .whereArrayContainsAny("tags", userTagOrdinals.take(10))
            .get()
            .await()

    return querySnapshot.documents.map { doc -> documentToEvent(doc) }
  }

  private fun rankEventsByTagMatch(user: UserProfile, events: List<Event>): List<Pair<Event, Int>> {
    return events
        .map { event ->
          val commonTags = user.tags.intersect(event.tags)
          event to commonTags.size
        }
        .sortedByDescending { it.second }
  }

  /**
   * Deletes an event identified by eventId.
   *
   * @param eventId the ID of the event to delete.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun deleteEvent(eventId: String) {
    val event = db.collection(EVENTS_COLLECTION_PATH).document(eventId).get().await()
    if (event.exists()) {
      db.collection(EVENTS_COLLECTION_PATH).document(eventId).delete().await()
    } else {
      throw NoSuchElementException("No event with ID $eventId found")
    }
  }

  /**
   * Generates a new unique ID for an event.
   *
   * @return a new unique event ID as a [String].
   */
  override suspend fun getNewID(): String {
    return UUID.randomUUID().toString()
  }
}
