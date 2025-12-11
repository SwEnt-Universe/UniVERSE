package com.android.universe.ui.navigation

/**
 * Contains test tags for UI elements in the application. These tags are used in UI tests to find
 * and interact with specific Composable on the screen, making the tests more robust and readable.
 */
object NavigationTestTags {

  const val BACK_BUTTON = "GoBackButton"

  // SCREENS
  const val MAP_SCREEN = "MapScreen"
  const val MAP_INSTANCE_SCREEN = "MapInstanceScreen"
  const val EVENT_SCREEN = "EventsScreen"
  const val CHAT_SCREEN = "ChatScreen"
  const val CHAT_INSTANCE_SCREEN = "ChatInstanceScreen"
  const val PROFILE_SCREEN = "ProfileScreen"
  const val SIGN_IN_SCREEN = "SignInScreen"
  const val ADD_PROFILE_SCREEN = "AddProfileScreen"
  const val SEARCH_PROFILE_SCREEN = "SearchProfileScreen"
  const val SETTINGS_SCREEN = "SettingsScreen"
  const val SELECT_TAG_SCREEN_USER = "SelectTagScreenUser"
  const val SELECT_TAG_SCREEN_EVENT = "SelectTagScreenEvent"
  const val SELECT_TAG_SCREEN_EVENT_EDITION = "SelectTagScreenEventEdition"
  const val SELECT_TAG_SCREEN_SETTINGS = "SelectTagScreenSettings"
  const val EMAIL_VALIDATION_SCREEN = "EmailValidationScreen"
  const val EVENT_CREATION_SCREEN = "EventCreationScreen"
  const val EVENT_EDITION_SCREEN = "EventEditionScreen"

  const val SELECT_LOCATION_SCREEN = "SelectLocationScreen"

  // BOTTOM NAVIGATION MENU
  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val CHAT_TAB = "ChatTab"
  const val EVENT_TAB = "EventsTab"
  const val MAP_TAB = "MapTab"
  const val PROFILE_TAB = "ProfileTab"
  const val COMMUNITY_TAB = "CommunityTab"

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
        is Tab.Community -> COMMUNITY_TAB
      }

  /**
   * Returns the test tag for a given [NavigationScreens] destination. This is used in UI tests to
   * verify that the correct screen is being displayed.
   *
   * @param screen The [NavigationScreens] destination for which to get the test tag.
   * @return The corresponding test tag as a [String].
   */
  fun getScreenTestTag(screen: NavigationScreens): String =
      when (screen) {
        is NavigationScreens.Map -> MAP_SCREEN
        is NavigationScreens.MapInstance -> MAP_INSTANCE_SCREEN
        is NavigationScreens.Event -> EVENT_SCREEN
        is NavigationScreens.Chat -> CHAT_SCREEN
        is NavigationScreens.Profile -> PROFILE_SCREEN
        is NavigationScreens.SignIn -> SIGN_IN_SCREEN
        is NavigationScreens.AddProfile -> ADD_PROFILE_SCREEN
        is NavigationScreens.SearchProfile -> SEARCH_PROFILE_SCREEN
        is NavigationScreens.Settings -> SETTINGS_SCREEN
        is NavigationScreens.SelectTagUser -> SELECT_TAG_SCREEN_USER
        is NavigationScreens.SelectTagEvent -> SELECT_TAG_SCREEN_EVENT
        is NavigationScreens.SelectTagEventEdition -> SELECT_TAG_SCREEN_EVENT_EDITION
        is NavigationScreens.EmailValidation -> EMAIL_VALIDATION_SCREEN
        is NavigationScreens.EventCreation -> EVENT_CREATION_SCREEN
        is NavigationScreens.EventEdition -> EVENT_EDITION_SCREEN
        is NavigationScreens.ChatInstance -> CHAT_INSTANCE_SCREEN
        is NavigationScreens.SelectTagUserSettings -> SELECT_TAG_SCREEN_SETTINGS
        is NavigationScreens.SelectLocation -> SELECT_LOCATION_SCREEN
      }
}
