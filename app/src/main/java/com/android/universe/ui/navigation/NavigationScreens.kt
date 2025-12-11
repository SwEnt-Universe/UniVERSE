package com.android.universe.ui.navigation

/**
 * Sealed class representing the different screens in the application's navigation graph. Each
 * object corresponds to a specific destination.
 *
 * @param route The unique route identifier for the navigation destination.
 * @param name The user-facing name or title of the screen.
 * @param isTopLevelDestination Flag indicating if the screen is a top-level destination, typically
 *   shown in a bottom navigation bar.
 */
sealed class NavigationScreens(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false,
) {
  object Map : NavigationScreens(route = "map", name = "Map", isTopLevelDestination = true)

  object MapInstance :
      NavigationScreens(
          route = "map/{eventId}/{creator}/{latitude}/{longitude}", name = "MapInstance")

  object Event : NavigationScreens(route = "event", name = "Event", isTopLevelDestination = true)

  object Chat : NavigationScreens(route = "chat", name = "Chat", isTopLevelDestination = true)

  object ChatInstance : NavigationScreens(route = "chat/{chatID}/{chatName}", name = "ChatInstance")

  object Profile :
      NavigationScreens(route = "profile", name = "Profile", isTopLevelDestination = true)

  object SignIn :
      NavigationScreens(route = "signIn", name = "Sign In", isTopLevelDestination = true)

  object EmailValidation : NavigationScreens(route = "emailValidation", name = "Email Validation")

  object AddProfile : NavigationScreens(route = "addProfile", name = "Add Profile")

  object SearchProfile : NavigationScreens(route = "searchProfile", name = "Search Profile")

  object Settings : NavigationScreens(route = "settings/{uid}", name = "Settings")

  object SelectTagUser : NavigationScreens(route = "selectTagUser", name = "Select Tag user")

  object SelectTagEvent : NavigationScreens(route = "selectTagEvent", name = "Select Tag event")

  object EventCreation : NavigationScreens(route = "eventCreation", name = "EventCreation")

  object SelectLocation : NavigationScreens(route = "selectLocation", name = "Select Location")

  object SelectTagUserSettings :
      NavigationScreens(route = "selectTagUserSettings", name = "Select Tag user settings")
}
