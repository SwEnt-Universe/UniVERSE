package com.android.universe.ui.navigation

/**
 * Contains test tags for UI elements in the application. These tags are used in UI tests to find
 * and interact with specific Composables on the screen, making the tests more robust and readable.
 */
object NavigationTestTags {

  const val GO_BACK_BUTTON = "GoBackButton"

  // SCREENS
  const val MAP_SCREEN = "MapScreen"
  const val EVENT_SCREEN = "EventsScreen"
  const val CHAT_SCREEN = "ChatScreen"
  const val PROFILE_SCREEN = "ProfileScreen"
  const val SIGN_IN_SCREEN = "SignInScreen"
  const val ADD_PROFILE_SCREEN = "AddProfileScreen"
  const val SETTINGS_SCREEN = "SettingsScreen"
  const val SELECT_TAG_SCREEN = "SelectTagScreen"

  // BOTTOM NAVIGATION MENU
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val CHAT_TAB = "ChatTab"
  const val EVENT_TAB = "EventsTab"
  const val MAP_TAB = "MapTab"
  const val PROFILE_TAB = "ProfileTab"

  /**
   * Returns the appropriate test tag for a given [Tab] in the bottom navigation menu. This allows
   * tests to dynamically find the correct tab based on the navigation state.
   *
   * @param tab The [Tab] for which to get the test tag.
   */
  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.Chat -> CHAT_TAB
        is Tab.Map -> MAP_TAB
        is Tab.Profile -> PROFILE_TAB
        is Tab.Event -> EVENT_TAB
      }

  fun getScreenTestTag(screen: NavigationScreens): String =
      when (screen) {
        is NavigationScreens.Map -> MAP_SCREEN
        is NavigationScreens.Event -> EVENT_SCREEN
        is NavigationScreens.Chat -> CHAT_SCREEN
        is NavigationScreens.Profile -> PROFILE_SCREEN
        is NavigationScreens.SignIn -> SIGN_IN_SCREEN
        is NavigationScreens.AddProfile -> ADD_PROFILE_SCREEN
        is NavigationScreens.Settings -> SETTINGS_SCREEN
        is NavigationScreens.SelectTag -> SELECT_TAG_SCREEN
      }
}
