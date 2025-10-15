package com.android.universe.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Represents a tab in the bottom navigation bar. Each tab has a destination screen, an icon for its
 * unselected state, and an icon for its selected state.
 *
 * @property destination The [NavigationScreens] entry that this tab navigates to.
 * @property icon The [ImageVector] to display when the tab is not selected.
 * @property iconSelected The [ImageVector] to display when the tab is selected.
 */
sealed class Tab(
    val destination: NavigationScreens,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
  /** Represents the 'Map' tab. */
  object Map : Tab(NavigationScreens.Map, Icons.Outlined.Explore, Icons.Filled.Explore)

  /** Represents the 'Event' tab. */
  object Event : Tab(NavigationScreens.Event, Icons.Outlined.Event, Icons.Filled.Event)

  /** Represents the 'Profile' tab. */
  object Profile :
      Tab(NavigationScreens.Profile, Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)

  /** Represents the 'Chat' tab. */
  object Chat :
      Tab(NavigationScreens.Chat, Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat)
}

/** A predefined list of [Tab] objects that will be displayed in the bottom navigation bar. */
val tabs =
    listOf(
        Tab.Chat,
        Tab.Map,
        Tab.Event,
        Tab.Profile,
    )

/**
 * The bottom navigation bar Composable for the application. It displays a set of tabs that the user
 * can interact with to navigate between the main screens.
 *
 * @param selectedTab The currently selected [Tab].
 * @param onTabSelected A callback function that is invoked when a tab is selected. It receives the
 *   selected [Tab] as an argument.
 */
@Composable
fun NavigationBottomMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
  NavigationBar(
      modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
  ) {
    tabs.forEach { tab ->
      val selected = tab == selectedTab
      NavigationBarItem(
          icon = {
            Icon(
                imageVector = if (selected) tab.iconSelected else tab.icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp))
          },
          selected = false,
          // false to hide an highlight around the icon if it's selected, since we visualize the
          // selected tab with an filled icon
          onClick = { onTabSelected(tab) },
          modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab)))
    }
  }
}

/**
 * A preview Composable for the [NavigationBottomMenu]. It shows the component with a default
 * selected tab.
 */
@Preview
@Composable
fun NavigationBottomMenuPreview() {
  val selectedTab: MutableState<Tab> = remember { mutableStateOf(Tab.Map) }
  NavigationBottomMenu(selectedTab = selectedTab.value, onTabSelected = { selectedTab.value = it })
}
