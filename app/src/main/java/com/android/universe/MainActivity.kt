package com.android.universe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.credentials.CredentialManager
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
import com.android.universe.ui.profileCreation.AddProfileScreen
import com.android.universe.ui.profileSettings.SettingsScreen
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.signIn.SignInViewModel
import com.android.universe.ui.theme.SampleAppTheme
import com.google.firebase.auth.FirebaseAuth

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
fun UniverseApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context)
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  var user = FirebaseAuth.getInstance().currentUser
  val startDestination =
      if (user == null) NavigationScreens.SignIn.name
      else NavigationScreens.Map.route

  val onTabSelected = { tab: Tab -> navigationActions.navigateTo(tab.destination) }

  NavHost(navController = navController, startDestination = startDestination) {
    navigation(
        route = NavigationScreens.SignIn.name,
        startDestination = NavigationScreens.SignIn.route,
    ) {
      composable(NavigationScreens.SignIn.route) {
        SignInScreen(
            onSignedIn = {
                            user = FirebaseAuth.getInstance().currentUser
                            if (!user!!.isAnonymous) navigationActions.navigateTo(NavigationScreens.AddProfile)
                            else navigationActions.navigateTo(NavigationScreens.Map)
                         },
            credentialManager = credentialManager)
      }
    }
    navigation(
        startDestination = NavigationScreens.AddProfile.route,
        route = NavigationScreens.AddProfile.name,
    )
    {
       composable(NavigationScreens.AddProfile.route) { AddProfileScreen(user!!.uid) }
    }
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
            uid = "0",
            onTabSelected = onTabSelected,
            onEditProfileClick = { uid ->
              navController.navigate(NavigationScreens.Settings.route.replace("{uid}", uid))
            })
      }
    }
    composable(
        route = NavigationScreens.Settings.route,
        arguments = listOf(navArgument("uid") { type = NavType.StringType })) { backStackEntry ->
          val uid = backStackEntry.arguments?.getString("uid") ?: "0"
          SettingsScreen(
              uid = uid,
              onBack = {
                navController.popBackStack(NavigationScreens.Profile.route, inclusive = false)
              },
          )
        }
  }
}
