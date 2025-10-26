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
            (map["tags"] as? List<Number>)?.map { ordinal -> Tag.entries[ordinal.toInt()] }?.toSet()
                ?: emptySet())
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
      Event(
          id = doc.getString("id") ?: "",
          title = doc.getString("title") ?: "",
          description = doc.getString("description"),
          date = doc.getString("date")?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
          tags =
              (doc.get("tags") as? List<Number>)
                  ?.map { ordinal -> Tag.entries[ordinal.toInt()] }
                  ?.toSet() ?: emptySet(),
          creator =
              mapToUserProfile(
                  doc.get("creator") as? Map<String, Any?>
                      ?: throw Exception("Creator data missing")),
          participants =
              (doc.get("participants") as? List<Map<String, Any?>>)
                  ?.map { mapToUserProfile(it) }
                  ?.toSet() ?: emptySet(),
          location =
              (doc.get("location") as? Map<String, Any?>)?.let { mapToLocation(it) }
                  ?: Location(0.0, 0.0))
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
