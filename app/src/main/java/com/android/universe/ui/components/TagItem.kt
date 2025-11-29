package com.android.universe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.IconDark
import com.android.universe.ui.theme.IconLight
import com.android.universe.ui.theme.LocalIsDarkTheme
import com.android.universe.ui.theme.TagSelectedBorderDark
import com.android.universe.ui.theme.TagSelectedBorderLight
import com.android.universe.ui.theme.tagColor

/** Contain the tag for the tests. */
object TagItemTestTag {
  /** Give the tag for a button according to the tag displayName. */
  fun tagButton(tag: Tag): String {
    return "Button" + tag.displayName
  }

  /** Give the tag for a text according to the tag displayName. */
  fun tagText(tag: Tag): String {
    return "Text" + tag.displayName
  }

  /** Give the tag for a icon according to the tag displayName. */
  fun tagIcon(tag: Tag): String {
    return "Icon" + tag.displayName
  }
}

/** Contain the dimensions used specially in this composable. */
object TagItemDefaults {
  const val HEIGHT_TAG = 36f
  const val WIDTH_TAG = 110f
  val SizeIcon = 18.dp
  val SelectedBorderWidth = 3.dp
}

/**
 * A composable that displays a single tag using the LiquidButton.
 *
 * This tag can optionally be selectable, and visually reacts to its selection state:
 * - The background color changes according to the tag’s category and whether it is selected.
 * - A border is added when the tag is selected.
 * - A check icon is shown when selected.
 *
 * The component handles its own internal selection state, and triggers external callbacks when
 * selected or deselected.
 *
 * @param tag The [Tag] model containing name and category information.
 * @param isSelectable Whether the tag can be interacted with. If `false`, click interaction is
 *   disabled.
 * @param onSelect Callback invoked when the tag transitions from unselected → selected.
 * @param onDeSelect Callback invoked when the tag transitions from selected → unselected.
 * @param modifier Optional [Modifier] applied to the TagItem layout.
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
  val isDark = LocalIsDarkTheme.current
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
      contentPadding = 4.dp,
      modifier =
          modifier
              .testTag(TagItemTestTag.tagButton(tag))
              .then(
                  if (isSelected)
                      Modifier.border(
                          width = TagItemDefaults.SelectedBorderWidth,
                          color =
                              if (isDark) {
                                TagSelectedBorderDark
                              } else {
                                TagSelectedBorderLight
                              },
                          shape = CapsuleLarge)
                  else Modifier)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              tag.displayName,
              fontSize = MaterialTheme.typography.labelSmall.fontSize,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(TagItemTestTag.tagText(tag)))
          if (isSelected) {
            Spacer(modifier = Modifier.width(Dimensions.SpacerSmall))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint =
                    if (isDark) {
                      IconDark
                    } else {
                      IconLight
                    },
                modifier =
                    Modifier.testTag(TagItemTestTag.tagIcon(tag)).size(TagItemDefaults.SizeIcon))
          }
        }
      }
}
