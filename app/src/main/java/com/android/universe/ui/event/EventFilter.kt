import com.android.universe.ui.event.EventUIState
import com.android.universe.ui.search.SearchEngine

internal fun filterEvents(events: List<EventUIState>, query: String): List<EventUIState> {
  if (query.isBlank()) return events

  return events.filter { event ->
    val fields = listOf(event.title, event.description, event.creator) + event.tags
    fields.any { field ->
      field.contains(query, ignoreCase = true) || SearchEngine.fuzzyMatch(field, query)
    }
  }
}
