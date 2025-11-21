package com.android.universe.ui.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.ui.theme.Dimensions

object SearchTestTags {
  const val SEARCH_BAR = "search_bar"
}

/**
 * A reusable search bar component
 *
 * ## Features
 * - Displays an `OutlinedTextField` with a search icon and placeholder text.
 * - Accepts user input and reports changes via [onQueryChange].
 * - Single-line field to keep search compact.
 * - Automatically expands to full width and adds top padding to avoid overlap with status bar.
 * - Exposes a test tag ([SearchTestTags.SEARCH_BAR]) for UI testing.
 *
 * @param query The current text value displayed inside the search bar.
 * @param onQueryChange Callback invoked whenever the user types or clears text.
 * @param modifier Optional modifier for external layout and positioning.
 * @param placeholder Placeholder text shown when the field is empty.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search events..."
) {
  OutlinedTextField(
      value = query,
      onValueChange = onQueryChange,
      singleLine = true,
      placeholder = { Text(placeholder) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search_icon") },
      shape = RoundedCornerShape(Dimensions.RoundedCornerMedium),
      colors =
          OutlinedTextFieldDefaults.colors(
              // BACKGROUND
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedContainerColor = MaterialTheme.colorScheme.surface,

              // TEXT & PLACEHOLDER
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
              focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),

              // BORDER
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
          ),
      modifier =
          modifier
              .fillMaxWidth()
              .padding(top = Dimensions.PaddingPutBelowStatusbar)
              .testTag(SearchTestTags.SEARCH_BAR))
}
