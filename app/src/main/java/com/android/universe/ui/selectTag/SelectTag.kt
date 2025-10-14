package com.android.universe.ui.selectTag

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.Tag
import com.android.universe.model.tagsCanton
import com.android.universe.model.tagsInterest
import com.android.universe.model.tagsMusic
import com.android.universe.model.tagsSport
import com.android.universe.model.tagsTransport
import kotlinx.coroutines.launch

object SelectTagsScreenTestTags {
  const val INTEREST_TAGS = "InterestTags"
  const val SPORT_TAGS = "SportTags"
  const val MUSIC_TAGS = "MusicTags"
  const val TRANSPORT_TAGS = "TransportTags"
  const val CANTON_TAGS = "CantonTags"
  const val SELECTED_TAGS = "SelectedTags"
  const val SAVE_BUTTON = "SaveButton"
  const val DIVIDER = "Divider"
  const val DELETE_ICON = "DeleteIcon"
  const val TAG_BUTTON_PREFIX = "Button_"
  const val SELECTED_TAG_BUTTON_PREFIX = "Button_Selected_"
}

/**
 * Composable that displays a group of selectable tags.
 *
 * Each tag can be selected or deselected by the user. Selected tags are displayed differently (with
 * a check icon and a border) and unselected tags use the group's color. Tags are displayed in a row
 * and automatically wrap to the next line when needed.
 *
 * @param name The name of the tag group.
 * @param tagList The list of tags to display.
 * @param selectedTags A mutable state holding the list of currently selected tags. Clicking a tag
 *   will update this state.
 * @param color The color of unselected tags (default is purple).
 * @param onTagSelect Callback invoked when a tag is selected.
 * @param onTagReSelect Callback invoked when a tag is deselected.
 * @param modifier The modifier to apply to the composable
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagGroup(
    name: String,
    tagList: List<Tag>,
    selectedTags: List<Tag>,
    color: Color = Color(0xFF6650a4),
    onTagSelect: (Tag) -> Unit = {},
    onTagReSelect: (Tag) -> Unit = {},
    modifier: Modifier = Modifier
) {
  Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
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
          modifier =
              Modifier.testTag("${SelectTagsScreenTestTags.TAG_BUTTON_PREFIX}${tag.displayName}")
                  .padding(4.dp),
          border = if (isSelected) BorderStroke(2.dp, Color(0xFF546E7A)) else null,
          colors = ButtonDefaults.buttonColors(containerColor = buttonColor)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(tag.displayName)
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

/** Composable that displays a horizontal line to visually divide sections on the tag screen. */
@Composable
fun SectionDivider() {
  HorizontalDivider(
      modifier = Modifier.testTag(SelectTagsScreenTestTags.DIVIDER).padding(vertical = 8.dp),
      thickness = 1.dp,
      color = Color.Black)
}

/**
 * Object holding predefined colors for different tag categories.
 *
 * Each property corresponds to a specific tag group and defines the color used to display tags of
 * that group.
 */
object TagColors {
  val Interest = Color(0xFFB39DDB)
  val Sport = Color(0xFFA5D6A7)
  val Music = Color(0xFF90A4AE)
  val Transport = Color(0xFF80CBC4)
  val Canton = Color(0xFF546E7A)
}

/**
 * Composable screen that displays tags organized by topic and allows the user to select them.
 *
 * Tags are grouped into the following topics: Interest, Sport, Music, Transport, and Canton. Each
 * topic is displayed with a title and a distinct color.
 *
 * Users can:
 * - Select a tag by clicking on it (selected tags turn grey and show a check icon).
 * - Deselect a tag by clicking it again in the main list or by clicking the trash icon in the
 *   selected tags section at the bottom.
 * - Save their selected tags using the "Save Tags" button, which updates their profile.
 *
 * Selected tags are displayed in a horizontal row at the bottom of the screen, allowing scrolling
 * if necessary.
 */
@Composable
fun SelectTagScreen(
    selectedTagOverview: SelectTagViewModel = viewModel(),
    username: String,
    navigateOnSave: () -> Unit = {}
) {
  LaunchedEffect(username) { selectedTagOverview.loadTags(username) }
  val selectedTags by selectedTagOverview.uiStateTags.collectAsState()
  Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
    LazyColumn(modifier = Modifier.testTag("LazyColumnTags").weight(1f)) {
      item {
        TagGroup(
            "Interest:",
            tagsInterest,
            selectedTags,
            TagColors.Interest,
            { tag -> selectedTagOverview.addTag(tag) },
            { tag -> selectedTagOverview.deleteTag(tag) },
            Modifier.testTag(SelectTagsScreenTestTags.INTEREST_TAGS))
      }
      item { SectionDivider() }
      item {
        TagGroup(
            "Sport:",
            tagsSport,
            selectedTags,
            TagColors.Sport,
            { tag -> selectedTagOverview.addTag(tag) },
            { tag -> selectedTagOverview.deleteTag(tag) },
            Modifier.testTag(SelectTagsScreenTestTags.SPORT_TAGS))
      }
      item { SectionDivider() }
      item {
        TagGroup(
            "Music:",
            tagsMusic,
            selectedTags,
            TagColors.Music,
            { tag -> selectedTagOverview.addTag(tag) },
            { tag -> selectedTagOverview.deleteTag(tag) },
            Modifier.testTag(SelectTagsScreenTestTags.MUSIC_TAGS))
      }
      item { SectionDivider() }
      item {
        TagGroup(
            "Transport:",
            tagsTransport,
            selectedTags,
            TagColors.Transport,
            { tag -> selectedTagOverview.addTag(tag) },
            { tag -> selectedTagOverview.deleteTag(tag) },
            Modifier.testTag(SelectTagsScreenTestTags.TRANSPORT_TAGS))
      }
      item { SectionDivider() }
      item {
        TagGroup(
            "Canton:",
            tagsCanton,
            selectedTags,
            TagColors.Canton,
            { tag -> selectedTagOverview.addTag(tag) },
            { tag -> selectedTagOverview.deleteTag(tag) },
            Modifier.testTag(SelectTagsScreenTestTags.CANTON_TAGS))
      }
      item { SectionDivider() }
    }
    if (selectedTags.isNotEmpty()) {
      LazyRow(modifier = Modifier.testTag(SelectTagsScreenTestTags.SELECTED_TAGS)) {
        items(selectedTags.toList()) { tag ->
          Button(
              onClick = {},
              modifier =
                  Modifier.testTag(
                      "${SelectTagsScreenTestTags.SELECTED_TAG_BUTTON_PREFIX}${tag.displayName}")) {
                Text(tag.displayName)
              }
          IconButton(
              onClick = { selectedTagOverview.deleteTag(tag) },
              modifier = Modifier.testTag(SelectTagsScreenTestTags.DELETE_ICON).size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.height(16.dp),
                )
              }
        }
      }
    }
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
          scope.launch {
            selectedTagOverview.saveTags(username)
            navigateOnSave()
          }
        },
        modifier =
            Modifier.testTag(SelectTagsScreenTestTags.SAVE_BUTTON).fillMaxWidth().padding(4.dp)) {
          Text("Save Tags")
        }
  }
}
