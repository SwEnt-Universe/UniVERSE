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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.universe.model.tag.Tag
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.LocalIsDarkTheme
import com.android.universe.ui.theme.TagSelectedBorderDark
import com.android.universe.ui.theme.TagSelectedBorderLight
import com.android.universe.ui.theme.tagColor

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
 * @param isSelectable Whether the tag can be interacted with. If `false`, click interaction is disabled.
 * @param onSelect Callback invoked when the tag transitions from unselected → selected.
 * @param onDeSelect Callback invoked when the tag transitions from selected → unselected.
 * @param modifier Optional [Modifier] applied to the TagItem layout.
 */
@Composable
fun TagItem(tag: Tag, isSelectable: Boolean, onSelect: () -> Unit, onDeSelect: () -> Unit, modifier: Modifier = Modifier){
    val isSelected = remember { mutableStateOf(false) }
    val buttonColor by animateColorAsState(targetValue = tagColor(category = tag.category.displayName, isSelected = isSelected.value))
    val isDark = LocalIsDarkTheme.current
    LiquidButton(
        onClick = {
            isSelected.value = !isSelected.value
            if (isSelected.value) {
            onDeSelect()
        }else{
            onSelect()
        }
                  },
        enabled = isSelectable,
        isInteractive = isSelectable,
        height = 10f,
        width = 10f,
        color = buttonColor,
        content = {
            Row {
                Text(tag.displayName)
                if (isSelected.value) {
                    Spacer(modifier = Modifier.width(Dimensions.SpacerSmall))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        modifier = modifier.then(
            if (isSelected.value) Modifier.border(width = 2.dp, color =
                if (isDark){
                    TagSelectedBorderDark
                }else{
                    TagSelectedBorderLight
                }
                , shape = CapsuleLarge)
            else Modifier
        )
    )
}