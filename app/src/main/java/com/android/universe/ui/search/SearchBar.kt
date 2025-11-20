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
      shape = RoundedCornerShape(Dimensions.RoundedCorner),
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
              .testTag(SearchTestTags.SEARCH_BAR))
}
