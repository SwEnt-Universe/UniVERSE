package com.android.universe.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Displays a labeled group of selectable tags, rendered as clickable buttons in a responsive
 * horizontal flow layout.
 *
 * Each tag button visually indicates its selection state:
 * - Unselected tags use the given [color] background.
 * - Selected tags appear gray with a checkmark icon and a highlighted border.
 *
 * When a tag is clicked:
 * - If it was not previously selected, [onTagSelect] is invoked.
 * - If it was already selected, [onTagReSelect] is invoked (allowing deselection or custom behavior).
 *
 * This composable is typically used to categorized tags such as user interests,
 * hobbies, or filters within a profile or settings screen.
 *
 * ### Example:
 * ```
 * TagGroup(
 *   name = "Interests",
 *   tagList = listOf("Music", "Art", "Travel"),
 *   selectedTags = listOf("Music"),
 *   onTagSelect = { println("Selected tag: $it") },
 *   onTagReSelect = { println("Deselected tag: $it") }
 * )
 * ```
 *
 * @param name Optional title displayed above the tag group (e.g., "Interests"). If empty, no title is shown.
 * @param tagList The full list of tag names to display as buttons.
 * @param selectedTags The subset of [tagList] that are currently selected.
 * @param color The background color for unselected tag buttons. Defaults to a purple accent (0xFF6650a4).
 * @param onTagSelect Callback triggered when a tag is newly selected.
 * @param onTagReSelect Callback triggered when a selected tag is clicked again.
 * @param modifier The [Modifier] to be applied to the root [FlowRow] container.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagGroup(
    name: String,
    tagList: List<String>,
    selectedTags: List<String>,
    color: Color = Color(0xFF6650a4),
    onTagSelect: (String) -> Unit = {},
    onTagReSelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  if (name.isNotEmpty()) {
    Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
  }
  FlowRow(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
    tagList.forEach { tag ->
      val isSelected = selectedTags.contains(tag)
      val buttonColor by animateColorAsState(targetValue = if (isSelected) Color.Gray else color)
      Button(
          onClick = {
            if (isSelected) {
              onTagReSelect(tag)
            } else {
              onTagSelect(tag)
            }
          },
          modifier = Modifier.padding(4.dp),
          border = if (isSelected) BorderStroke(2.dp, Color(0xFF546E7A)) else null,
          colors = ButtonDefaults.buttonColors(containerColor = buttonColor)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(tag)
              if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp))
              }
            }
          }
    }
  }
}
