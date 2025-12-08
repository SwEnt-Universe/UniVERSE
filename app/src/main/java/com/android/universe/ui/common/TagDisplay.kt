package com.android.universe.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.universe.model.tag.Tag
import com.android.universe.ui.components.CategoryItem
import com.android.universe.ui.components.CategoryItemDefaults
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.components.TagItem
import com.android.universe.ui.components.TagItemDefaults
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.Dimensions.PaddingMedium

/**
 * Contains test tag identifiers for [TagColumn], [TagRow], and related components. These strings
 * are used to locate UI elements during automated testing.
 */
object TagGroupTestTag {
  /**
   * Generates a unique test tag for a TagColumn based on its content.
   *
   * @param tags The list of tags contained in the column.
   * @return A unique string identifier.
   */
  fun tagColumn(tags: List<Tag>): String {
    val key = tags.sortedBy { it.displayName }.joinToString("_") { it.displayName }
    return "Column$key"
  }

  /**
   * Generates a unique test tag for a TagRow based on its content.
   *
   * @param tags The list of tags contained in the row.
   * @return A unique string identifier.
   */
  fun tagRow(tags: List<Tag>): String {
    val key = tags.sortedBy { it.displayName }.joinToString("_") { it.displayName }
    return "Row$key"
  }
}

/** Contains default dimension and style constants used by [TagGroup], [TagColumn], and [TagRow]. */
object TagGroupDefaults {
  val DefaultHeight = 300.dp
  val DefaultWidth = 500.dp
  val DefaultOuterPaddingH = 8.dp
  val DefaultOuterPaddingV = 12.dp
  val DefaultInterPaddingH = 8.dp
  val DefaultInterPaddingV = 12.dp
  val titleFontSize = 28.sp
  val titleDefaultLineSize = 32.sp
}

/**
 * A Composable that displays a vertical column of tags.
 *
 * This component renders a scrollable list of [TagItem]s arranged vertically. It supports
 * interactive selection and can optionally apply a visual fade effect at the top and bottom edges
 * to indicate scrolling content.
 *
 * @param tags The list of [Tag] objects to display.
 * @param modifierTags Modifier to be applied to each individual [TagItem].
 * @param modifierBox Modifier to be applied to the outer container of the column.
 * @param heightTag The fixed height for each [TagItem]. Defaults to [TagItemDefaults.HEIGHT_TAG].
 * @param heightList The fixed height of the entire column. Defaults to
 *   [TagGroupDefaults.DefaultHeight].
 * @param isSelectable Whether the tags can be interacted with (selected/deselected).
 * @param isSelected A lambda that returns true if a given [Tag] is currently selected.
 * @param onTagSelect Callback invoked when an unselected tag is clicked.
 * @param onTagReSelect Callback invoked when a selected tag is clicked again (e.g., to deselect).
 * @param tagElement Optional lambda to generate a custom test tag for individual tag items.
 * @param state The [LazyListState] to control or observe the scrolling state.
 * @param fade If true, applies a transparency gradient fade to the top and bottom edges.
 * @param fadeHeight The height of the fade effect in Dp. Defaults to 10% of [heightList].
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

  LazyColumn(
      state = state,
      verticalArrangement = Arrangement.spacedBy(TagGroupDefaults.DefaultInterPaddingV),
      contentPadding = PaddingValues(vertical = TagGroupDefaults.DefaultInterPaddingV),
      modifier =
          modifierBox
              .height(heightList)
              .testTag(TagGroupTestTag.tagColumn(tags))
              .fadingEdge(visible = fade, fadeSize = fadeHeight)) {
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
 * A Composable that displays a horizontal row of tags.
 *
 * This component renders a scrollable list of [TagItem]s arranged horizontally. It supports
 * interactive selection and can optionally apply a visual fade effect at the left and right edges
 * to indicate scrolling content.
 *
 * @param tags The list of [Tag] objects to display.
 * @param modifierTags Modifier to be applied to each individual [TagItem].
 * @param modifierBox Modifier to be applied to the outer container of the row.
 * @param heightTag The fixed height for each [TagItem]. Defaults to [TagItemDefaults.HEIGHT_TAG].
 * @param widthList The fixed width of the entire row. Defaults to [TagGroupDefaults.DefaultWidth].
 * @param isSelectable Whether the tags can be interacted with (selected/deselected).
 * @param isSelected A lambda that returns true if a given [Tag] is currently selected.
 * @param onTagSelect Callback invoked when an unselected tag is clicked.
 * @param onTagReSelect Callback invoked when a selected tag is clicked again.
 * @param tagElement Optional lambda to generate a custom test tag for individual tag items.
 * @param state The [LazyListState] to control or observe the scrolling state.
 * @param fade If true, applies a transparency gradient fade to the left and right edges.
 * @param fadeWidth The width of the fade effect in Dp. Defaults to 10% of [widthList].
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

  LazyRow(
      state = state,
      horizontalArrangement = Arrangement.spacedBy(TagGroupDefaults.DefaultInterPaddingH),
      contentPadding = PaddingValues(horizontal = TagGroupDefaults.DefaultInterPaddingH),
      modifier =
          modifierBox
              .width(widthList)
              .testTag(TagGroupTestTag.tagRow(tags))
              .fadingEdge(visible = fade, fadeSize = fadeWidth, isVertical = false)) {
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
 * Displays a labeled group of selectable tags organized in a dynamic grid layout.
 *
 * This component is designed for screens that require selecting multiple tags from a category
 * (e.g., selecting interests). It features:
 * - Dynamic Columns: Automatically calculates the number of columns based on the available width.
 * - Scrolling: Supports vertical scrolling if the content exceeds the maximum height.
 * - Visual Effects: Includes a top and bottom fade effect to indicate scrolling, and uses
 *   [LiquidBox] for the container background.
 * - Responsive Layout: Tags are distributed evenly across columns, and spacers are used to maintain
 *   alignment in the last row.
 *
 * @param modifierColumn Modifier applied to the inner Column holding the title and list.
 * @param modifierFlowRow Modifier applied to the outer [LiquidBox] container. This is typically
 *   used for applying test tags to the whole group.
 * @param height The maximum height of the tag group container. The container will shrink if content
 *   is smaller, but will scroll if content exceeds this height. Defaults to
 *   [TagGroupDefaults.DefaultHeight].
 * @param heightTag The fixed height for individual [TagItem]s.
 * @param interPaddingH Horizontal spacing between columns.
 * @param interPaddingV Vertical spacing between rows.
 * @param outerPaddingH Horizontal padding inside the group container.
 * @param outerPaddingV Vertical padding inside the group container.
 * @param title Optional title text displayed at the top of the group.
 * @param tagList The list of [Tag] objects to display.
 * @param selectedTags The list of currently selected tags.
 * @param isSelectable Whether tags can be selected/deselected.
 * @param onTagSelect Callback invoked when an unselected tag is clicked.
 * @param onTagReSelect Callback invoked when a selected tag is clicked again.
 * @param displayText Whether to display the title text.
 * @param tagElement Optional lambda to generate custom test tags for items.
 * @param fade Whether to enable the top/bottom scroll fade effect.
 * @param fadeHeight The height of the fade effect in Dp. Defaults to 10% of [height].
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
  val scrollState = rememberScrollState()

  LiquidBox(
      modifier = Modifier.fillMaxWidth().heightIn(max = height).then(modifierFlowRow),
      shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = modifierColumn.fillMaxWidth().padding(horizontal = outerPaddingH),
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (displayText) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = outerPaddingV, bottom = Dimensions.PaddingSmall),
                    fontSize = TagGroupDefaults.titleFontSize,
                    lineHeight = TagGroupDefaults.titleDefaultLineSize,
                    textAlign = TextAlign.Center)
              }

              BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                val itemWidth = TagItemDefaults.WIDTH_TAG.dp
                val columns =
                    maxOf(1, ((maxWidth + interPaddingH) / (itemWidth + interPaddingH)).toInt())

                val chunkedTags = tagList.chunked(columns)

                Box(
                    modifier =
                        Modifier.fillMaxWidth().fadingEdge(visible = fade, fadeSize = fadeHeight)) {
                      Column(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .verticalScroll(scrollState)
                                  .padding(
                                      vertical = if (fade) fadeHeight * 0.5f else outerPaddingV),
                          verticalArrangement = Arrangement.spacedBy(interPaddingV)) {
                            chunkedTags.forEach { rowTags ->
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  horizontalArrangement = Arrangement.spacedBy(interPaddingH)) {
                                    rowTags.forEach { tag ->
                                      Box(
                                          modifier = Modifier.weight(1f),
                                          contentAlignment = Alignment.Center) {
                                            TagItem(
                                                tag = tag,
                                                heightTag = heightTag,
                                                isSelectable = isSelectable,
                                                isSelected = selectedTags.contains(tag),
                                                onSelect = { onTagSelect(tag) },
                                                onDeSelect = { onTagReSelect(tag) },
                                                modifier =
                                                    Modifier.then(
                                                        if (tagElement != null)
                                                            Modifier.testTag(tagElement(tag))
                                                        else Modifier))
                                          }
                                    }

                                    val missingItems = columns - rowTags.size
                                    repeat(missingItems) { Spacer(modifier = Modifier.weight(1f)) }
                                  }
                            }
                          }
                    }
              }
            }
      }
}

/**
 * Applies a fading transparency effect to the start and end edges of a component.
 *
 * This modifier creates a visual "fade out" effect, typically used to indicate scrollable content
 * that extends beyond the visible area. It uses an offscreen compositing layer and a
 * [BlendMode.DstIn] gradient to mask the content's alpha channel.
 *
 * @param visible Whether the fade effect should be applied. If false, the modifier returns the
 *   original instance.
 * @param fadeSize The size (height or width depending on orientation) of the fade gradient in [Dp].
 * @param isVertical Determines the direction of the fade. True for top/bottom fading (vertical),
 *   false for left/right fading (horizontal). Defaults to true.
 * @return A [Modifier] with the fading effect applied.
 */
private fun Modifier.fadingEdge(
    visible: Boolean,
    fadeSize: Dp,
    isVertical: Boolean = true
): Modifier {
  if (!visible) return this

  return this.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
      .drawWithContent {
        drawContent()
        val fadePx = fadeSize.toPx()
        val length = if (isVertical) size.height else size.width

        if (length <= 0f || fadePx <= 0f) return@drawWithContent

        val startRatio = (fadePx / length).coerceIn(0f, 0.5f)
        val endRatio = ((length - fadePx) / length).coerceIn(0.5f, 1f)

        val transparent = Color.Transparent
        val opaque = Color.Black

        val brush =
            if (isVertical) {
              Brush.verticalGradient(
                  0f to transparent, startRatio to opaque, endRatio to opaque, 1f to transparent)
            } else {
              Brush.horizontalGradient(
                  0f to transparent, startRatio to opaque, endRatio to opaque, 1f to transparent)
            }
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
      }
}

object CategoryRowTestTag {
  val ROW_TAG = "CategoryRowRowTestTag"
}

/**
 * A composable for the category row.
 *
 * @param modifier Modifier for the Box that contains the row. It already has a fading edge effect
 *   applied as well as a max filled width
 * @param isSelected A lambda that returns true if a given [Tag.Category] is currently selected.
 * @param onSelect Callback invoked when an unselected tag is clicked.
 * @param onDeSelect Callback invoked when a selected tag is clicked again.
 */
@Composable
fun CategoryRow(
    modifier: Modifier = Modifier,
    isSelected: (Tag.Category) -> Boolean,
    onSelect: (Tag.Category) -> Unit,
    onDeSelect: (Tag.Category) -> Unit
) {
  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .fadingEdge(
                  visible = true,
                  fadeSize = (CategoryItemDefaults.HEIGHT_CAT * 0.5f).dp,
                  isVertical = false)) {
        Row(
            modifier =
                Modifier.testTag(CategoryRowTestTag.ROW_TAG)
                    .horizontalScroll(state = rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PaddingMedium)) {
              Spacer(modifier = Modifier.width(PaddingMedium))
              for (category in Tag.Category.entries) {
                CategoryItem(
                    category = category,
                    isSelectable = true,
                    isSelected = isSelected(category),
                    onSelect = onSelect,
                    onDeSelect = onDeSelect)
              }
              Spacer(modifier = Modifier.width(PaddingMedium))
            }
      }
}
