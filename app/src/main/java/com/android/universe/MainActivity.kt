package com.android.universe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.universe.resources.C
import com.android.universe.ui.event.EventScreen
import com.android.universe.ui.map.MapScreen
import com.android.universe.ui.navigation.NavigationActions
import com.android.universe.ui.navigation.NavigationPlaceholderScreen
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.profile.UserProfileScreen
import com.android.universe.ui.profileSettings.SettingsScreen
import com.android.universe.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              UniverseApp()
            }
      }
    }
  }
}

/**
 * The main composable for the Universe app.
 *
 * This composable sets up the navigation for the app using a [NavHost].
 */
@Composable
fun UniverseApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val startDestination = NavigationScreens.Map.route
  // TODO: verify that user is authenticated once the signIn is done.

  val onTabSelected = { tab: Tab -> navigationActions.navigateTo(tab.destination) }

  NavHost(navController = navController, startDestination = startDestination) {
    navigation(
        startDestination = NavigationScreens.Map.route,
        route = NavigationScreens.Map.name,
    ) {
      composable(NavigationScreens.Map.route) { MapScreen(onTabSelected) }
    }

    navigation(
        startDestination = NavigationScreens.Event.route,
        route = NavigationScreens.Event.name,
    ) {
      composable(NavigationScreens.Event.route) { EventScreen(onTabSelected) }
    }

    navigation(
        startDestination = NavigationScreens.Chat.route,
        route = NavigationScreens.Chat.name,
    ) {
      composable(NavigationScreens.Chat.route) {
        NavigationPlaceholderScreen(
            title = NavigationScreens.Chat.name,
            selectedTab = Tab.Chat,
            onTabSelected = onTabSelected,
            testTag = NavigationTestTags.CHAT_SCREEN,
        )
      }
    }

    navigation(
        startDestination = NavigationScreens.Profile.route,
        route = NavigationScreens.Profile.name,
    ) {
      composable(NavigationScreens.Profile.route) {
        UserProfileScreen(
            username = "emma",
            onTabSelected = onTabSelected,
            onEditProfileClick = { username ->
              navController.navigate(
                  NavigationScreens.Settings.route.replace("{username}", username))
            })
      }
    }
    composable(
        route = NavigationScreens.Settings.route,
        arguments = listOf(navArgument("username") { type = NavType.StringType })) { backStackEntry
          ->
          val username = backStackEntry.arguments?.getString("username") ?: "emma"
          SettingsScreen(
              username = username,
              onBack = {
                navController.popBackStack(NavigationScreens.Profile.route, inclusive = false)
              },
          )
        }
  }
}
