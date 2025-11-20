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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  const val BUTTON = "Button"
  const val TEXT = "Text"
  const val ICON = "Icon"
}

/** Contain the dimensions used specially in this composable. */
object TagItemDefaults {
  const val HEIGHT_TAG = 30f
  const val WIDTH_TAG = 160f
  val FontSizeText = 13.sp
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
    isAlreadySelected: Boolean = false,
    onSelect: (Tag) -> Unit,
    onDeSelect: (Tag) -> Unit
) {
  val isSelected = remember { mutableStateOf(isAlreadySelected) }
  LaunchedEffect(isAlreadySelected) { isSelected.value = isAlreadySelected }
  val buttonColor by
      animateColorAsState(
          targetValue =
              tagColor(category = tag.category.displayName, isSelected = isSelected.value))
  val isDark = LocalIsDarkTheme.current
  LiquidButton(
      onClick = {
        isSelected.value = !isSelected.value
        if (isSelected.value) {
          onSelect(tag)
        } else {
          onDeSelect(tag)
        }
      },
      enabled = isSelectable,
      isInteractive = isSelectable,
      height = heightTag,
      width = TagItemDefaults.WIDTH_TAG,
      color = buttonColor,
      content = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              tag.displayName,
              fontSize = TagItemDefaults.FontSizeText,
              modifier = Modifier.testTag(TagItemTestTag.TEXT))
          if (isSelected.value) {
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
                modifier = Modifier.testTag(TagItemTestTag.ICON).size(TagItemDefaults.SizeIcon))
          }
        }
      },
      modifier =
          modifier
              .testTag(TagItemTestTag.BUTTON)
              .then(
                  if (isSelected.value)
                      Modifier.border(
                          width = TagItemDefaults.SelectedBorderWidth,
                          color =
                              if (isDark) {
                                TagSelectedBorderDark
                              } else {
                                TagSelectedBorderLight
                              },
                          shape = CapsuleLarge)
                  else Modifier))
}
