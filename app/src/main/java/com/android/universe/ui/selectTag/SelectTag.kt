package com.android.universe.ui.selectTag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.tag.Tag
import com.android.universe.ui.common.TagGroup
import com.android.universe.ui.navigation.FlowBottomMenu
import com.android.universe.ui.navigation.FlowTab
import com.android.universe.ui.theme.Dimensions

object SelectTagsScreenTestTags {
  const val MUSIC_TAGS = "MusicTags"
  const val SPORT_TAGS = "SportTags"
  const val FOOD_TAGS = "FoodTags"
  const val ART_TAGS = "ArtTags"
  const val TRAVEL_TAGS = "TravelTags"
  const val GAMES_TAGS = "GamesTags"
  const val TECHNOLOGY_TAGS = "TechnologyTags"
  const val TOPIC_TAGS = "TopicTags"
  const val SELECTED_TAGS = "SelectedTags"
  const val SAVE_BUTTON = "SaveButton"
  const val DELETE_ICON = "DeleteIcon"
  const val TAG_BUTTON_PREFIX = "Button_"
  const val SELECTED_TAG_BUTTON_PREFIX = "Button_Selected_"
  const val LAZY_COLUMN = "LazyColumnTags"

  fun unselectedTag(tag: Tag): String = "$TAG_BUTTON_PREFIX${tag.displayName.replace(" ", "_")}"

  fun selectedTag(tag: Tag): String =
      "$SELECTED_TAG_BUTTON_PREFIX${tag.displayName.replace(" ", "_")}"
}

/** Composable that displays a horizontal line to visually divide sections on the tag screen. */
@Composable
fun SectionDivider() {
  Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
}

/**
 * Composable screen that displays tags organized by topic and allows the user to select them.
 *
 * Tags are grouped into categories defined by [Tag.Category]. Each category is displayed with a
 * title and a distinct color. Users can:
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
    selectTagMode: SelectTagMode = SelectTagMode.USER_PROFILE,
    selectedTagOverview: SelectTagViewModel = viewModel(),
    uid: String,
    onBack: () -> Unit = {},
    navigateOnSave: () -> Unit = {}
) {
  LaunchedEffect(uid) {
    selectedTagOverview.mode = selectTagMode
    selectedTagOverview.eventTagRepositoryObserving()
    selectedTagOverview.loadTags(uid)
  }
  val selectedTags by selectedTagOverview.selectedTags.collectAsState()

  Scaffold(
      containerColor = Color.Transparent,
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        FlowBottomMenu(
            flowTabs =
                listOf(
                    FlowTab.Back(onClick = onBack),
                    FlowTab.Confirm(
                        onClick = {
                          selectedTagOverview.saveTags(uid)
                          navigateOnSave()
                        },
                        enabled = true)))
      }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.testTag(SelectTagsScreenTestTags.LAZY_COLUMN).fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = 12.dp,
                    end = 12.dp,
                    bottom =
                        innerPadding.calculateBottomPadding() - Dimensions.PaddingExtraLarge)) {
              items(Tag.Category.entries) { category ->
                TagGroup(
                    title =
                        when (category) {
                          Tag.Category.MUSIC ->
                              "Select the music genres and the events you enjoy..."
                          Tag.Category.SPORT -> "Choose the sports you're into..."
                          Tag.Category.FOOD -> "Select the food and drink experiences you love..."
                          Tag.Category.ART -> "Pick the types of art you connect with..."
                          Tag.Category.TRAVEL -> "Choose the travel styles you’re interested in..."
                          Tag.Category.GAMES -> "Select the games you like to play..."
                          Tag.Category.TECHNOLOGY ->
                              "Choose the tech topics you’re interested in..."
                          Tag.Category.TOPIC -> "Pick the topics that interest you..."
                        },
                    tagList = Tag.getTagsForCategory(category),
                    selectedTags = selectedTags,
                    onTagSelect = { tag -> selectedTagOverview.addTag(tag) },
                    onTagReSelect = { tag -> selectedTagOverview.deleteTag(tag) },
                    modifierFlowRow =
                        Modifier.testTag(
                            when (category) {
                              Tag.Category.MUSIC -> SelectTagsScreenTestTags.MUSIC_TAGS
                              Tag.Category.SPORT -> SelectTagsScreenTestTags.SPORT_TAGS
                              Tag.Category.FOOD -> SelectTagsScreenTestTags.FOOD_TAGS
                              Tag.Category.ART -> SelectTagsScreenTestTags.ART_TAGS
                              Tag.Category.TRAVEL -> SelectTagsScreenTestTags.TRAVEL_TAGS
                              Tag.Category.GAMES -> SelectTagsScreenTestTags.GAMES_TAGS
                              Tag.Category.TECHNOLOGY -> SelectTagsScreenTestTags.TECHNOLOGY_TAGS
                              Tag.Category.TOPIC -> SelectTagsScreenTestTags.TOPIC_TAGS
                            }),
                    tagElement = { tag -> SelectTagsScreenTestTags.unselectedTag(tag) })
                SectionDivider()
              }
            }
      }
}

@Preview
@Composable
fun SelectTagPreview() {
  SelectTagScreen(uid = "0")
}
