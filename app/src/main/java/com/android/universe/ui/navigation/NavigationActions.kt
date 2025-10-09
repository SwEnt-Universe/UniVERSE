package com.android.universe.ui.navigation

import androidx.navigation.NavHostController

/**
 * A class that provides navigation actions for the application.
 *
 * @param navController The navigation controller to use for navigation.
 */
class NavigationActions(
    private val navController: NavHostController,
) {

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  fun navigateTo(screen: NavigationScreens) {
    // If the user is already on destination screen, do nothing
    if (currentRoute() == screen.route) {
      return
    }

    navController.navigate(screen.route) {
      if (screen.isTopLevelDestination) {
        launchSingleTop = true
        popUpTo(screen.route) { inclusive = true }
      }
      restoreState = screen.isTopLevelDestination
    }
  }

  /** Navigate back to the previous screen. */
  fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
