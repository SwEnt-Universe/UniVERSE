package com.android.universe.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.theme.Dimensions

object TopBarTestTags {
  const val TOP_BAR_TITLE = "topBarTitle"
}

private val shape =
    RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = Dimensions.RoundedCorner,
        bottomEnd = Dimensions.RoundedCorner)

/**
 * A custom top bar with a "liquid" shape, curving at the bottom corners. This Composable is
 * designed to be used as a screen's top app bar.
 *
 * It uses [LiquidBox] to achieve its distinct shape and provides slots for a navigation icon and a
 * title.
 *
 * @param navigationIcon A composable lambda for the navigation icon, typically a back button.
 *   Defaults to an empty composable.
 * @param title A composable lambda for the title content, usually a [TopBarTitle].
 */
@Composable
fun LiquidTopBar(navigationIcon: @Composable (() -> Unit) = {}, title: @Composable (() -> Unit)) {
  LiquidBox(modifier = Modifier.fillMaxWidth(), shape = shape) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Dimensions.PaddingExtraLarge),
        verticalAlignment = Alignment.CenterVertically) {
          navigationIcon()
          title()
        }
  }
}

/**
 * A standard back button for the top bar.
 *
 * @param onClick The lambda to be executed when the button is clicked.
 * @param modifier The [Modifier] to be applied to this button.
 */
@Composable
fun TopBarBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(onClick = onClick, modifier = modifier.testTag(NavigationTestTags.BACK_BUTTON)) {
    Icon(
        Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = MaterialTheme.colorScheme.onSurface)
  }
}

/**
 * A composable function that displays a title for a top bar. The text will be truncated with an
 * ellipsis if it's too long to fit in a single line.
 *
 * @param text The string to be displayed as the title.
 * @param modifier The modifier to be applied to the Text composable. Defaults to an empty
 *   [Modifier].
 */
@Composable
fun TopBarTitle(text: String, modifier: Modifier = Modifier) {
  Text(
      text = text,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = modifier.testTag(TopBarTestTags.TOP_BAR_TITLE))
}
