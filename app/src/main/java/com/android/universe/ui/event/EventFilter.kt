import com.android.universe.ui.event.EventUIState
import com.android.universe.ui.search.SearchEngine

/**
 * Filters a list of [EventUIState] items based on a search [query].
 *
 * This function performs case–insensitive matching across the key textual fields of an event:
 * - title
 * - description
 * - creator full name
 * - tags
 *
 * Filtering succeeds if **any** of these fields:
 *  - contains the query string (case–insensitive), **or**
 *  - matches according to [SearchEngine.fuzzyMatch], which allows tolerant matching for typos
 *    or near-miss input.
 *
 * If [query] is blank, the original list of events is returned without modification.
 *
 * @param events The complete list of event UI models to search within.
 * @param query The search text entered by the user.
 * @return A filtered list of events whose fields match the query.
 */
internal fun filterEvents(events: List<EventUIState>, query: String): List<EventUIState> {
  if (query.isBlank()) return events

  return events.filter { event ->
    val fields = listOf(event.title, event.description, event.creator) + event.tags
    fields.any { field ->
      field.contains(query, ignoreCase = true) || SearchEngine.fuzzyMatch(field, query)
    }
  }
}
