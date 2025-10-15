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

  object Event : NavigationScreens(route = "event", name = "Event", isTopLevelDestination = true)

  object Chat : NavigationScreens(route = "chat", name = "Chat", isTopLevelDestination = true)

  object Profile :
      NavigationScreens(route = "profile", name = "Profile", isTopLevelDestination = true)

  object SignIn : NavigationScreens(route = "signIn", name = "Sign In")

  object AddProfile : NavigationScreens(route = "addProfile", name = "Add Profile")

  object Settings : NavigationScreens(route = "settings/{username}", name = "Settings")

  object SelectTag : NavigationScreens(route = "selectTag", name = "Select Tag")
}
