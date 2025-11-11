package com.android.universe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kyant.backdrop.Backdrop

/**
 * A placeholder screen that displays a title and an optional bottom navigation menu. This is used
 * for screens that are not yet implemented.
 *
 * @param title The title to be displayed on the screen.
 * @param selectedTab The currently selected [Tab] in the bottom navigation menu.
 * @param onTabSelected A callback invoked when a tab is selected.
 * @param enableBottomBar A flag to control the visibility of the bottom navigation bar.
 * @param testTag An optional test tag for the [Scaffold].
 */
@Composable
fun NavigationPlaceholderScreen(
    title: String,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    enableBottomBar: Boolean = true,
    testTag: String? = null,
    backdrop: Backdrop
) {
  Scaffold(
      modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
      bottomBar = { if (enableBottomBar) NavigationBottomMenu(selectedTab, onTabSelected, backdrop) }) {
          padding ->
        Text(text = title, modifier = Modifier.padding(padding))
      }
}
