package com.android.universe.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.universe.model.tag.Tag
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.TagItem
import com.android.universe.ui.components.TagItemDefaults
import com.android.universe.ui.theme.Dimensions

/** Contain the tag for the tests. */
object TagGroupTestTag {
  fun tagColumn(tags: List<Tag>): String {
    val key = tags.sortedBy { it.displayName }.joinToString("_") { it.displayName }
    return "Column$key"
  }

  fun tagRow(tags: List<Tag>): String {
    val key = tags.sortedBy { it.displayName }.joinToString("_") { it.displayName }
    return "Row$key"
  }
}

/** Contain the dimensions used specially in this composable. */
object TagGroupDefaults {
  val DefaultHeight = 300.dp
  val DefaultWidth = 500.dp
  val DefaultOuterPaddingH = 8.dp
  val DefaultOuterPaddingV = 12.dp
  val DefaultInterPaddingH = 8.dp
  val DefaultInterPaddingV = 12.dp
  val CornerShapeDp = 16.dp
  val titleFontSize = 28.sp
  val titleDefaultLineSize = 32.sp
}

/**
 * A Composable that displays a vertical column of tags with selectable options and visual effects.
 *
 * This function creates a column of tags (`LazyColumn`) inside a `Box`. It can show a custom
 * background, allow tag selection/re-selection, and add "fade" effects at the top and bottom edges.
 *
 * @param tags List of [Tag] items to display.
 * @param modifierTags Modifier applied to each individual tag item.
 * @param modifierBox Modifier applied to the `Box` containing the LazyColumn and fade effects.
 * @param modifierFade Modifier applied to the fade boxes (top and bottom).
 * @param heightTag Height of an individual tag (default [TagItemDefaults.HEIGHT_TAG]).
 * @param heightList Total height of the tag column (default [TagGroupDefaults.DefaultHeight]).
 * @param isSelectable Whether tags can be selected (default `true`).
 * @param isSelected Function that takes a [Tag] and returns `true` if it is currently selected.
 * @param onTagSelect Callback invoked when a tag is selected.
 * @param onTagReSelect Callback invoked when a tag that is already selected is clicked again.
 * @param tagElement Optional function to convert a [Tag] into a string for testing or labeling.
 * @param state State of the LazyColumn (default `rememberLazyListState()`), allows programmatic
 *   scrolling.
 * @param background If `true`, applies a rounded background around the tag column with a color that
 *   adapts to the current theme.
 * @param cornerShapeDp Corner radius for the rounded background (default
 *   [TagGroupDefaults.CornerShapeDp]).
 * @param fade If `true`, shows a "fade" effect (gradient) at the top and bottom edges of the tag
 *   column.
 * @param fadeHeight Height of the fade effect (default 10% of `heightList`).
 */
@Composable
fun TagColumn(
    tags: List<Tag>,
    modifierTags: Modifier = Modifier,
    modifierBox: Modifier = Modifier,
    heightTag: Float = TagItemDefaults.HEIGHT_TAG,
    heightList: Dp = TagGroupDefaults.DefaultHeight,
    isSelectable: Boolean = true,
    isSelected: (Tag) -> Boolean,
    onTagSelect: (Tag) -> Unit = {},
    onTagReSelect: (Tag) -> Unit = {},
    tagElement: ((Tag) -> String)? = null,
    state: LazyListState = rememberLazyListState(),
    fade: Boolean = true,
    fadeHeight: Dp = heightList * 0.1f
) {
    val density = LocalDensity.current
    val fadeHeightPx = with(density) { fadeHeight.toPx() }

  LazyColumn(
      state = state,
      verticalArrangement = Arrangement.spacedBy(TagGroupDefaults.DefaultInterPaddingV),
      contentPadding = PaddingValues(vertical = TagGroupDefaults.DefaultInterPaddingV),
      modifier =
          modifierBox
              .height(heightList)
              .testTag(TagGroupTestTag.tagColumn(tags))
              .then(
                  if (fade) {
                      Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                          .drawWithContent {
                              drawContent()
                              val transparent = Color.Black.copy(alpha = 0f)
                              val opaque = Color.Black
                              val brush =
                                  Brush.verticalGradient(
                                      0f to transparent,
                                      (fadeHeightPx / size.height) to opaque,
                                      ((size.height - fadeHeightPx) / size.height) to opaque,
                                      1f to transparent
                                  )
                              drawRect(brush = brush, blendMode = BlendMode.DstIn)
                          }
                  } else Modifier)
  ) {
        items(tags) { tag ->
          TagItem(
              tag = tag,
              heightTag = heightTag,
              isSelectable = isSelectable,
              isSelected = isSelected(tag),
              onSelect = { tag -> onTagSelect(tag) },
              onDeSelect = { tag -> onTagReSelect(tag) },
              modifier =
                  modifierTags.then(
                      if (tagElement != null) Modifier.testTag(tagElement(tag)) else Modifier))
        }
      }
}

/**
 * A Composable that displays a horizontal row of tags with selectable options and visual effects.
 *
 * This function creates a row of tags (`LazyRow`) inside a `Box`. It can show a custom background,
 * allow tag selection/re-selection, and add "fade" effects on the left and right edges.
 *
 * @param tags List of [Tag] items to display.
 * @param modifierTags Modifier applied to each individual tag item.
 * @param modifierBox Modifier applied to the `Box` containing the LazyRow and fade effects.
 * @param modifierFade Modifier applied to the fade boxes (left and right).
 * @param heightTag Height of an individual tag (default [TagItemDefaults.HEIGHT_TAG]).
 * @param widthList Total width of the tag row (default [TagGroupDefaults.DefaultWidth]).
 * @param isSelectable Whether tags can be selected (default `true`).
 * @param isSelected Function that takes a [Tag] and returns `true` if it is currently selected.
 * @param onTagSelect Callback invoked when a tag is selected.
 * @param onTagReSelect Callback invoked when a tag that is already selected is clicked again.
 * @param tagElement Optional function to convert a [Tag] into a string for testing or labeling.
 * @param state State of the LazyRow (default `rememberLazyListState()`), allows programmatic
 *   scrolling.
 * @param background If `true`, applies a rounded background around the tag row with a color that
 *   adapts to the current theme.
 * @param cornerShapeDp Corner radius for the rounded background (default
 *   [TagGroupDefaults.CornerShapeDp]).
 * @param fade If `true`, shows a "fade" effect (gradient) on the left and right edges of the tag
 *   row.
 * @param fadeWidth Width of the fade (default 10% of `widthList`).
 */
@Composable
fun TagRow(
    tags: List<Tag>,
    modifierTags: Modifier = Modifier,
    modifierBox: Modifier = Modifier,
    heightTag: Float = TagItemDefaults.HEIGHT_TAG,
    widthList: Dp = TagGroupDefaults.DefaultWidth,
    isSelectable: Boolean = true,
    isSelected: (Tag) -> Boolean,
    onTagSelect: (Tag) -> Unit = {},
    onTagReSelect: (Tag) -> Unit = {},
    tagElement: ((Tag) -> String)? = null,
    state: LazyListState = rememberLazyListState(),
    fade: Boolean = true,
    fadeWidth: Dp = widthList * 0.1f
) {
    val density = LocalDensity.current
    val fadeWidthPx = with(density) { fadeWidth.toPx() }

    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(TagGroupDefaults.DefaultInterPaddingH),
        contentPadding = PaddingValues(horizontal = TagGroupDefaults.DefaultInterPaddingH),
        modifier =
            modifierBox.width(widthList)
                .testTag(TagGroupTestTag.tagRow(tags))
                .then(
                    if (fade) {
                        Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                val transparent = Color.Black.copy(alpha = 0f)
                                val opaque = Color.Black
                                val brush =
                                    Brush.horizontalGradient(
                                        0f to transparent,
                                        (fadeWidthPx / size.width) to opaque,
                                        ((size.width - fadeWidthPx) / size.width) to opaque,
                                        1f to transparent
                                    )
                                drawRect(brush = brush, blendMode = BlendMode.DstIn)
                            }
                    } else Modifier
                )
    ) {
        items(tags) { tag ->
            TagItem(
                tag = tag,
                heightTag = heightTag,
                isSelectable = isSelectable,
                isSelected = isSelected(tag),
                onSelect = { tag -> onTagSelect(tag) },
                onDeSelect = { tag -> onTagReSelect(tag) },
                modifier =
                    modifierTags.then(
                        if (tagElement != null) Modifier.testTag(tagElement(tag)) else Modifier
                    )
            )
        }
    }
}

/**
 * Displays a labeled group of selectable tags, rendered as clickable buttons in a responsive
 * horizontal flow layout.
 *
 * Each tag button visually indicates its selection state: selected tags are highlighted with a
 * border and check icon. Supports scrolling if the content overflows. Top and bottom fade effects
 * make the tags disappear smoothly when scrolling.
 *
 * When a tag is clicked:
 * - If it was not previously selected, [onTagSelect] is invoked.
 * - If it was already selected, [onTagReSelect] is invoked (allowing deselection or custom
 *   behavior).
 *
 * This composable is typically used to categorized tags such as user interests, hobbies, or filters
 * within a profile or settings screen.
 *
 * @param modifierColumn Modifier applied to the outer Column of the tag group.
 * @param modifierFlowRow Modifier applied to the FlowRow containing the tags.
 * @param height The height of the tag container. Defaults to [TagGroupDefaults.DefaultHeight].
 * @param interPaddingH Horizontal spacing between tags inside the FlowRow. Defaults to
 *   [TagGroupDefaults.DefaultInterPaddingH].
 * @param interPaddingV Vertical spacing between tags inside the FlowRow. Defaults to
 *   [TagGroupDefaults.DefaultInterPaddingV].
 * @param outerPaddingH Horizontal padding of the outer Column. Defaults to
 *   [TagGroupDefaults.DefaultOuterPaddingH].
 * @param outerPaddingV Vertical padding of the outer Column. Defaults to
 *   [TagGroupDefaults.DefaultOuterPaddingV].
 * @param title Optional title displayed above the tag group. If empty or blank, no title is shown.
 * @param tagList The list of tags to display.
 * @param selectedTags The list of tags that are currently selected.
 * @param isSelectable If true, tags can be selected/deselected; otherwise, tags are read-only.
 * @param onTagSelect Callback invoked when a tag is selected.
 * @param onTagReSelect Callback invoked when a selected tag is clicked again (deselected).
 * @param displayText If true, the group name is displayed above the tags.
 * @param tagElement Optional lambda that returns a unique string for each tag, useful for testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagGroup(
    modifierColumn: Modifier = Modifier,
    modifierFlowRow: Modifier = Modifier,
    height: Dp = TagGroupDefaults.DefaultHeight,
    heightTag: Float = TagItemDefaults.HEIGHT_TAG,
    interPaddingH: Dp = TagGroupDefaults.DefaultInterPaddingH,
    interPaddingV: Dp = TagGroupDefaults.DefaultInterPaddingV,
    outerPaddingH: Dp = TagGroupDefaults.DefaultOuterPaddingH,
    outerPaddingV: Dp = TagGroupDefaults.DefaultOuterPaddingV,
    title: String,
    tagList: List<Tag>,
    selectedTags: List<Tag>,
    isSelectable: Boolean = true,
    onTagSelect: (Tag) -> Unit = {},
    onTagReSelect: (Tag) -> Unit = {},
    displayText: Boolean = true,
    tagElement: ((Tag) -> String)? = null,
    fade: Boolean = true,
    fadeHeight: Dp = height * 0.1f
) {
    val density = LocalDensity.current
    val fadeHeightPx = with(density) { fadeHeight.toPx() }
    val scrollState = rememberScrollState()

    LiquidBox(
        modifier = Modifier.fillMaxWidth().heightIn(max = height),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = modifierColumn
                .fillMaxWidth()
                .padding(horizontal = outerPaddingH),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (displayText) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = outerPaddingV, bottom = Dimensions.PaddingSmall),
                    fontSize = TagGroupDefaults.titleFontSize,
                    lineHeight = TagGroupDefaults.titleDefaultLineSize,
                    textAlign = TextAlign.Center
                )
            }

            val chunkedTags = tagList.chunked(3)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .then(
                        if (fade) {
                            Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()
                                    val brush = Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        (fadeHeightPx / size.height) to Color.Black,
                                        ((size.height - fadeHeightPx) / size.height) to Color.Black,
                                        1f to Color.Transparent
                                    )
                                    drawRect(brush = brush, blendMode = BlendMode.DstIn)
                                }
                        } else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = if (fade) fadeHeight * 0.5f else outerPaddingV),
                    verticalArrangement = Arrangement.spacedBy(interPaddingV)
                ) {
                    chunkedTags.forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(interPaddingH)
                        ) {
                            rowTags.forEach { tag ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TagItem(
                                        tag = tag,
                                        heightTag = heightTag,
                                        isSelectable = isSelectable,
                                        isSelected = selectedTags.contains(tag),
                                        onSelect = { onTagSelect(tag) },
                                        onDeSelect = { onTagReSelect(tag) },
                                        modifier = Modifier.then(
                                            if (tagElement != null) Modifier.testTag(tagElement(tag))
                                            else Modifier
                                        )
                                    )
                                }
                            }

                            val missingItems = 3 - rowTags.size
                            repeat(missingItems) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
