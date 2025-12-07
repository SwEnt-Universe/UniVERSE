package com.android.universe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.universe.model.tag.Tag
import com.android.universe.ui.theme.tagColor

/**
 * Contains test tag generators for [TagItem] components. These helpers ensure consistent naming for
 * UI tests when locating specific tags by their display name.
 */
object TagItemTestTag {
  /**
   * Generates the test tag for the clickable button surface of a specific [Tag].
   *
   * @param tag The tag object.
   * @return A string identifier, e.g., "ButtonMusic".
   */
  fun tagButton(tag: Tag): String {
    return "Button" + tag.displayName
  }

  /**
   * Generates the test tag for the text label inside a specific [Tag].
   *
   * @param tag The tag object.
   * @return A string identifier, e.g., "TextMusic".
   */
  fun tagText(tag: Tag): String {
    return "Text" + tag.displayName
  }
}

/** Contains default dimension constants used by the [TagItem] composable. */
object TagItemDefaults {
  const val HEIGHT_TAG = 24f
  const val WIDTH_TAG = 110f
}

/**
 * A composable that displays a single interactive tag button.
 *
 * This component renders a specific [Tag] using a [LiquidButton]. It visually communicates the
 * tag's category through color and its state through opacity/tint changes.
 *
 * @param modifier Modifier to be applied to the button container.
 * @param tag The [Tag] data model containing the display name and category.
 * @param heightTag The height of the button in dp (float value). Defaults to
 *   [TagItemDefaults.HEIGHT_TAG].
 * @param isSelectable Whether the tag responds to clicks. If false, the button acts as a static
 *   label.
 * @param isSelected Whether the tag is currently selected. Affects the color tint.
 * @param onSelect Callback invoked when the user clicks an unselected tag.
 * @param onDeSelect Callback invoked when the user clicks a selected tag.
 */
@Composable
fun TagItem(
    modifier: Modifier = Modifier,
    tag: Tag,
    heightTag: Float = TagItemDefaults.HEIGHT_TAG,
    isSelectable: Boolean,
    isSelected: Boolean = false,
    onSelect: (Tag) -> Unit,
    onDeSelect: (Tag) -> Unit
) {
  val buttonColor by
      animateColorAsState(
          targetValue = tagColor(category = tag.category.displayName, isSelected = isSelected))

  LiquidButton(
      onClick = {
        if (isSelected) {
          onDeSelect(tag)
        } else {
          onSelect(tag)
        }
      },
      enabled = isSelectable,
      isInteractive = isSelectable,
      height = heightTag,
      width = TagItemDefaults.WIDTH_TAG,
      tint = buttonColor,
      disableBackdrop = true,
      contentPadding = 4.dp,
      modifier = modifier.testTag(TagItemTestTag.tagButton(tag))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              tag.displayName,
              fontSize = MaterialTheme.typography.labelSmall.fontSize,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(TagItemTestTag.tagText(tag)))
        }
      }
}

/**
 * Contains test tag generators for [CategoryItem] components. These helpers ensure consistent
 * naming for UI tests when locating specific tags by their display name.
 */
object CategoryItemTestTags {
  fun categoryButton(category: Tag.Category): String {
    return "CategoryButton" + category.displayName
  }

  fun categoryText(category: Tag.Category): String {
    return "Category" + category.displayName
  }
}

/** Contains default dimension constants used by the [CategoryItem] composable. */
object CategoryItemDefaults {
  const val HEIGHT_CAT = 32f
  const val WIDTH_CAT = 96f
}

/**
 * A composable that displays a single interactive category button similar to the TagItem. This
 * component renders a specific [Tag.Category] using a [LiquidButton]. It visually communicates the
 * category's name through color and its state through opacity/tint changes.
 *
 * @param modifier Modifier to be applied to the button container.
 * @param category The [Tag.Category] data model containing the display name
 * @param heightTag The height of the button in dp (float value). Defaults to
 *   [CategoryItemDefaults.HEIGHT_CAT]
 * @param isSelectable Whether the Category responds to clicks. If false, the button acts as a
 *   static label. By default it is true
 * @param isSelected Whether the Category is currently selected. Affects the color tint.
 * @param onSelect Callback invoked when the user clicks an unselected Category.
 * @param onDeSelect Callback invoked when the user clicks a selected Category.
 */
@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    category: Tag.Category,
    heightTag: Float = CategoryItemDefaults.HEIGHT_CAT,
    isSelectable: Boolean = true,
    isSelected: Boolean = false,
    onSelect: (Tag.Category) -> Unit,
    onDeSelect: (Tag.Category) -> Unit
) {
  val buttonColor by
      animateColorAsState(
          targetValue = tagColor(category = category.displayName, isSelected = isSelected))

  LiquidButton(
      onClick = {
        if (isSelected) {
          onDeSelect(category)
        } else {
          onSelect(category)
        }
      },
      enabled = isSelectable,
      isInteractive = isSelectable,
      height = heightTag,
      width = CategoryItemDefaults.WIDTH_CAT,
      tint = buttonColor,
      disableBackdrop = true,
      contentPadding = 4.dp,
      modifier = modifier.testTag(CategoryItemTestTags.categoryButton(category))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              category.displayName,
              fontSize = MaterialTheme.typography.labelSmall.fontSize,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(CategoryItemTestTags.categoryText(category)))
        }
      }
}
