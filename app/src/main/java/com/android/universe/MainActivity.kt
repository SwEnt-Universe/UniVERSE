package com.android.universe

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.resources.C
import com.android.universe.ui.emailVerification.EmailVerificationScreen
import com.android.universe.ui.event.EventScreen
import com.android.universe.ui.eventCreation.EventCreationScreen
import com.android.universe.ui.map.MapScreen
import com.android.universe.ui.navigation.NavigationActions
import com.android.universe.ui.navigation.NavigationPlaceholderScreen
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.navigation.resolveUserDestinationScreen
import com.android.universe.ui.profile.UserProfileScreen
import com.android.universe.ui.profileCreation.AddProfileScreen
import com.android.universe.ui.profileSettings.SettingsScreen
import com.android.universe.ui.selectTag.SelectTagScreen
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.theme.UniverseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      UniverseTheme {
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
  val userRepository = UserRepositoryProvider.repository
  // Hold the start destination in state
  var startDestination by remember { mutableStateOf<NavigationScreens?>(null) }
  LaunchedEffect(Unit) {
    startDestination = resolveUserDestinationScreen(userRepository = userRepository)
  }

  val onTabSelected = { tab: Tab -> navigationActions.navigateTo(tab.destination) }
  if (startDestination == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      LinearProgressIndicator()
    }
  } else {

    NavHost(navController = navController, startDestination = startDestination!!.name) {
      navigation(
          route = NavigationScreens.SignIn.name,
          startDestination = NavigationScreens.SignIn.route,
      ) {
        composable(NavigationScreens.SignIn.route) {
          SignInScreen(
              onSignedIn = {
                runBlocking {
                  val destination = resolveUserDestinationScreen(userRepository)
                  navigationActions.navigateTo(destination)
                }
              },
              credentialManager = credentialManager)
        }
      }
      navigation(
          startDestination = NavigationScreens.AddProfile.route,
          route = NavigationScreens.AddProfile.name,
      ) {
        composable(NavigationScreens.AddProfile.route) {
          AddProfileScreen(
              uid = Firebase.auth.currentUser!!.uid,
              navigateOnSave = { navigationActions.navigateTo(NavigationScreens.SelectTag) },
              onBack = {
                // Navigate back to Sign In
                navController.navigate(NavigationScreens.SignIn.route) {
                  popUpTo(NavigationScreens.AddProfile.route) { inclusive = true }
                }
              })
        }
      }

      navigation(
          startDestination = NavigationScreens.EmailValidation.route,
          route = NavigationScreens.EmailValidation.name,
      ) {
        composable(NavigationScreens.EmailValidation.route) {
          EmailVerificationScreen(
              user = Firebase.auth.currentUser!!,
              onSuccess = {
                coroutineScope.launch {
                  navigationActions.navigateTo(resolveUserDestinationScreen(userRepository))
                }
              },
              onBack = { navigationActions.navigateTo(NavigationScreens.SignIn) })
        }
      }

      navigation(
          route = NavigationScreens.SelectTag.name,
          startDestination = NavigationScreens.SelectTag.route) {
            composable(NavigationScreens.SelectTag.route) {
              SelectTagScreen(
                  uid = Firebase.auth.currentUser!!.uid,
                  navigateOnSave = { navigationActions.navigateTo(NavigationScreens.Map) })
            }
          }
      navigation(
          startDestination = NavigationScreens.Map.route,
          route = NavigationScreens.Map.name,
      ) {
        composable(NavigationScreens.Map.route) {
          MapScreen(
              uid = Firebase.auth.currentUser!!.uid,
              onTabSelected = onTabSelected,
              createEvent = { lat, lng -> navController.navigate("eventCreation/$lat/$lng") })
        }
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
              uid = Firebase.auth.currentUser!!.uid,
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
                onLogout = { navigationActions.navigateTo(NavigationScreens.SignIn) },
                clear = {
                  credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
                })
          }
      navigation(
          startDestination = NavigationScreens.EventCreation.route,
          route = NavigationScreens.EventCreation.name,
      ) {
        composable(
            route = NavigationScreens.EventCreation.route,
            arguments =
                listOf(
                    navArgument("latitude") { type = NavType.FloatType },
                    navArgument("longitude") { type = NavType.FloatType })) { backStackEntry ->
              val latitude = backStackEntry.arguments?.getFloat("latitude") ?: 0f
              val longitude = backStackEntry.arguments?.getFloat("longitude") ?: 0f

              EventCreationScreen(
                  location = Location(latitude.toDouble(), longitude.toDouble()),
                  onSave = { navController.popBackStack() })
            }
      }
    }
  }
}
