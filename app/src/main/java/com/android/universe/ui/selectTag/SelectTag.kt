package com.android.universe.ui.selectTag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

/**
 * Contains test tag identifiers for the Select Tag Screen. These tags are used by UI tests to
 * locate specific categories, buttons, and layout structures.
 */
object SelectTagsScreenTestTags {
  const val MUSIC_TAGS = "MusicTags"
  const val SPORT_TAGS = "SportTags"
  const val FOOD_TAGS = "FoodTags"
  const val ART_TAGS = "ArtTags"
  const val TRAVEL_TAGS = "TravelTags"
  const val GAMES_TAGS = "GamesTags"
  const val TECHNOLOGY_TAGS = "TechnologyTags"
  const val TOPIC_TAGS = "TopicTags"
  const val SAVE_BUTTON = "SaveButton"
  const val TAG_BUTTON_PREFIX = "Button_"
  const val LAZY_COLUMN = "LazyColumnTags"

  /**
   * Generates a test tag for a tag button in the main list. Renamed from 'unselectedTag' to
   * 'tagItem' as it represents the item regardless of selection state.
   *
   * @param tag The tag object.
   * @return A string identifier, e.g., "Button_Metal".
   */
  fun tagItem(tag: Tag): String = "$TAG_BUTTON_PREFIX${tag.displayName.replace(" ", "_")}"
}

/** A visual separator used to add vertical spacing between different [TagGroup] sections. */
@Composable
fun SectionDivider() {
  Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))
}

/**
 * A screen that allows users to view and select tags from various categorized groups.
 *
 * This screen displays a scrollable list of [TagGroup]s, one for each [Tag.Category]. It is used
 * for both User Profile creation/editing and Event creation, depending on the [selectTagMode].
 *
 * **Features:**
 * - **Categorization:** Tags are grouped by topics (Music, Sport, Food, etc.).
 * - **Selection:** Tapping a tag toggles its selection state.
 * - **Persistence:** The [SelectTagViewModel] manages the selection state and saves changes to the
 *   repository.
 * - **Navigation:** Uses a [Scaffold] with a [FlowBottomMenu] to provide "Back" and "Confirm"
 *   actions.
 *
 * @param selectTagMode Determines the context of the screen (e.g., [SelectTagMode.USER_PROFILE] or
 *   [SelectTagMode.EVENT_CREATION]), affecting which data source is used.
 * @param selectedTagOverview The ViewModel managing the selected tags state.
 * @param uid The unique identifier for the entity being edited (User ID or Event ID).
 * @param onBack Callback invoked when the user clicks the "Back" button in the bottom menu.
 * @param navigateOnSave Callback invoked after the tags have been successfully saved via the
 *   "Confirm" button.
 */
@Composable
fun SelectTagScreen(
    uid: String,
    selectTagMode: SelectTagMode = SelectTagMode.USER_PROFILE,
    onBack: () -> Unit = {},
    navigateOnSave: () -> Unit = {},
    selectedTagOverview: SelectTagViewModel = viewModel { SelectTagViewModel(uid, selectTagMode) },
) {
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
                          selectedTagOverview.saveTags()
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
                    tagElement = { tag -> SelectTagsScreenTestTags.tagItem(tag) })
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
