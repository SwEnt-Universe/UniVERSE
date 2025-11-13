package com.android.universe.model.event

import android.util.Log
import com.android.universe.model.location.Location
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
        "participants" to event.participants.toList(),
        "creator" to event.creator,
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
      // the participants list that have been casted safely.
      val participantsList = doc.get("participants").safeCastList<String>()
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
          creator = doc.getString("creator") ?: "",
          participants = participantsList.toSet(),
          location = mapToLocation(locationMap))
    } catch (e: Exception) {
      Log.e("EventRepositoryFirestore", "Error converting document to Event", e)
      throw e
    }
  }

  /**
   * Retrieves all events currently stored in the repository from the given source.
   *
   * @param source the Firestore [Source] to fetch data from (DEFAULT/SERVER/CACHE).
   * @return a list of [Event] objects.
   */
  override suspend fun getAllEvents(source: Source): List<Event> {
    val events = ArrayList<Event>()
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get(source).await()
    for (document in querySnapshot.documents) {
      val event = documentToEvent(document)
      events.add(event)
    }
    return events
  }

  /**
   * Retrieves an event by its ID from the given source.
   *
   * @param eventId the unique ID of the event.
   * @param source the Firestore [Source] to fetch data from (DEFAULT/SERVER/CACHE).
   * @return the [Event] associated with the given ID.
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  override suspend fun getEvent(eventId: String, source: Source): Event {
    val event = db.collection(EVENTS_COLLECTION_PATH).document(eventId).get(source).await()
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
   * @param source the [Source] from which to fetch the events (DEFAULT/CACHE/SERVER).
   * @return a list of suggested [Event] objects.
   */
  override suspend fun getSuggestedEventsForUser(user: UserProfile, source: Source): List<Event> {
    val matchedEvents = getEventsMatchingUserTags(user, source)
    val rankedEvents = rankEventsByTagMatch(user, matchedEvents)
    return rankedEvents.take(50).map { it.first }
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
   * Retrieves events from Firestore whose tag ordinals intersect with the provided user's tags.
   *
   * Limits the query to the first 10 tag ordinals because Firestore's `whereArrayContainsAny`
   * accepts up to 10 elements.
   *
   * @param user the [UserProfile] whose tags are used to match events.
   * @param source the [Source] from which to fetch the events (DEFAULT/CACHE/SERVER).
   * @return a list of [Event] objects that match at least one of the user's tags.
   */
  private suspend fun getEventsMatchingUserTags(user: UserProfile, source: Source): List<Event> {
    val userTagOrdinals = user.tags.map { it.ordinal }.shuffled()
    if (userTagOrdinals.isEmpty()) return emptyList()

    val querySnapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .whereArrayContainsAny("tags", userTagOrdinals.take(10))
            .get(source)
            .await()

    return querySnapshot.documents.map { doc -> documentToEvent(doc) }
  }

  /**
   * Ranks events based on the number of matching tags with the user's profile.
   *
   * @param user the [UserProfile] whose tags are used for ranking.
   * @param events the list of [Event] objects to rank.
   * @return a list of pairs containing the [Event] and its corresponding match score, sorted in
   *   descending order of match score.
   */
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
