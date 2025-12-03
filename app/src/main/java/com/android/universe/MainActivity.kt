package com.android.universe

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.core.graphics.scale
import androidx.core.view.WindowCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.universe.background.BackgroundSnapshotRepository
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.resources.C
import com.android.universe.ui.chat.ChatListScreen
import com.android.universe.ui.chat.ChatScreen
import com.android.universe.ui.common.UniverseBackgroundContainer
import com.android.universe.ui.emailVerification.EmailVerificationScreen
import com.android.universe.ui.event.EventScreen
import com.android.universe.ui.eventCreation.EventCreationScreen
import com.android.universe.ui.map.MapScreen
import com.android.universe.ui.navigation.NavigationActions
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.navigation.resolveUserDestinationScreen
import com.android.universe.ui.profile.UserProfileScreen
import com.android.universe.ui.profileCreation.AddProfile
import com.android.universe.ui.profileSettings.SettingsScreen
import com.android.universe.ui.selectTag.SelectTagMode
import com.android.universe.ui.selectTag.SelectTagScreen
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    // Enable edge-to-edge with auto-contrast (icons adapt to content/theme)
    enableEdgeToEdge()

    // Enable automatic index creation for Firestore (makes offline queries faster)
    Firebase.firestore.persistentCacheIndexManager?.apply { enableIndexAutoCreation() }

    setContent {
      UniverseTheme {
        val backgroundColor = Color.White
        val backdrop = rememberLayerBackdrop {
          drawRect(backgroundColor)
          drawContent()
        }
        CompositionLocalProvider(LocalLayerBackdrop provides backdrop) {
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
}

/**
 * The main composable for the Universe app.
 *
 * This composable sets up the navigation for the app using a [NavHost].
 */
@Composable
fun UniverseApp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context),
) {
  val authInstance = remember { FirebaseAuth.getInstance() }
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val userRepository = UserRepositoryProvider.repository
  val mainActivityScope = rememberCoroutineScope()
  BackgroundSnapshotRepository.loadInitialSnapshot(context)
  val res = BitmapFactory.decodeResource(context.resources, R.drawable.map_snapshot2)
  val bitmap =
      BackgroundSnapshotRepository.currentSnapshot
          ?: res.scale(
                  (res.width * Dimensions.ImageScale).toInt(),
                  (res.height * Dimensions.ImageScale).toInt())
              .asImageBitmap()
  // Hold the start destination in state
  var startDestination by remember { mutableStateOf<NavigationScreens?>(null) }
  LaunchedEffect(Unit) {
    startDestination = resolveUserDestinationScreen(userRepository = userRepository)
  }
  val onTabSelected = { tab: Tab -> navigationActions.navigateTo(tab.destination) }
  if (startDestination == null) {
    UniverseBackgroundContainer(bitmap = bitmap, contentAlignment = Alignment.Center) {
      LinearProgressIndicator()
    }
  } else {

    NavHost(navController = navController, startDestination = startDestination!!.name) {
      navigation(
          route = NavigationScreens.SignIn.name,
          startDestination = NavigationScreens.SignIn.route,
      ) {
        composable(NavigationScreens.SignIn.route) {
          var navigateToDestination by remember { mutableStateOf<NavigationScreens?>(null) }

          LaunchedEffect(navigateToDestination) {
            navigateToDestination?.let { destination -> navigationActions.navigateTo(destination) }
          }

          UniverseBackgroundContainer(bitmap) {
            SignInScreen(
                onSignedIn = {
                  mainActivityScope.launch {
                    navigateToDestination = resolveUserDestinationScreen(userRepository)
                  }
                },
                credentialManager = credentialManager)
          }
        }
      }
      navigation(
          startDestination = NavigationScreens.AddProfile.route,
          route = NavigationScreens.AddProfile.name,
      ) {
        composable(NavigationScreens.AddProfile.route) {
          UniverseBackgroundContainer(bitmap) {
            AddProfile(
                uid = authInstance.currentUser!!.uid,
                navigateOnSave = { navigationActions.navigateTo(NavigationScreens.SelectTagUser) },
                onBack = {
                  // Navigate back to Sign In
                  navController.navigate(NavigationScreens.SignIn.route) {
                    popUpTo(NavigationScreens.AddProfile.route) { inclusive = true }
                  }
                })
          }
        }
      }

      navigation(
          startDestination = NavigationScreens.EmailValidation.route,
          route = NavigationScreens.EmailValidation.name,
      ) {
        composable(NavigationScreens.EmailValidation.route) {
          var navigateToDestination by remember { mutableStateOf<NavigationScreens?>(null) }

          LaunchedEffect(navigateToDestination) {
            navigateToDestination?.let { destination -> navigationActions.navigateTo(destination) }
          }

          UniverseBackgroundContainer(bitmap) {
            EmailVerificationScreen(
                user = authInstance.currentUser!!,
                onSuccess = {
                  mainActivityScope.launch {
                    navigateToDestination = resolveUserDestinationScreen(userRepository)
                  }
                },
                onBack = { navigationActions.navigateTo(NavigationScreens.SignIn) })
          }
        }
      }

      navigation(
          route = NavigationScreens.SelectTagUser.name,
          startDestination = NavigationScreens.SelectTagUser.route) {
            composable(NavigationScreens.SelectTagUser.route) {
              UniverseBackgroundContainer(bitmap) {
                SelectTagScreen(
                    uid = authInstance.currentUser!!.uid,
                    navigateOnSave = { navigationActions.navigateTo(NavigationScreens.Map) },
                    onBack = { navigationActions.goBack() })
              }
            }
          }

      navigation(
          startDestination = NavigationScreens.Map.route,
          route = NavigationScreens.Map.name,
      ) {
        composable(NavigationScreens.Map.route) {
          MapScreen(
              uid = authInstance.currentUser!!.uid,
              onTabSelected = onTabSelected,
              onChatNavigate = { chatID, chatName ->
                navController.navigate(
                    route =
                        NavigationScreens.ChatInstance.route
                            .replace("{chatID}", chatID)
                            .replace("{chatName}", chatName)
                            .replace("{userID}", authInstance.currentUser!!.uid))
              },
              createEvent = { lat, lng -> navController.navigate("eventCreation/$lat/$lng") })
        }

        composable(
            route = NavigationScreens.MapInstance.route,
            arguments =
                listOf(
                    navArgument("eventId") { type = NavType.StringType },
                    navArgument("latitude") { type = NavType.FloatType },
                    navArgument("longitude") { type = NavType.FloatType })) { backStackEntry ->
              val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
              val lat = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
              val lng = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0

              MapScreen(
                  uid = authInstance.currentUser!!.uid,
                  onTabSelected = onTabSelected,
                  preselectedEventId = eventId,
                  preselectedLocation = Location(lat, lng),
                  onChatNavigate = { chatID, chatName ->
                    navController.navigate(
                        NavigationScreens.ChatInstance.route
                            .replace("{chatID}", chatID)
                            .replace("{chatName}", chatName)
                            .replace("{userID}", authInstance.currentUser!!.uid))
                  },
                  createEvent = { lat2, lng2 ->
                    navController.navigate("eventCreation/$lat2/$lng2")
                  })
            }
      }

      navigation(
          startDestination = NavigationScreens.Event.route,
          route = NavigationScreens.Event.name,
      ) {
        composable(NavigationScreens.Event.route) {
          UniverseBackgroundContainer(bitmap) {
            EventScreen(
                onTabSelected,
                uid = authInstance.currentUser!!.uid,
                onChatNavigate = { chatID, chatName ->
                  navController.navigate(
                      route =
                          NavigationScreens.ChatInstance.route
                              .replace("{chatID}", chatID)
                              .replace("{chatName}", chatName)
                              .replace("{userID}", authInstance.currentUser!!.uid))
                },
                onCardClick = { eventId: String, eventLocation: Location ->
                  navController.navigate(
                      NavigationScreens.MapInstance.route
                          .replace("{eventId}", eventId)
                          .replace("{latitude}", eventLocation.latitude.toFloat().toString())
                          .replace("{longitude}", eventLocation.longitude.toFloat().toString()))
                })
          }
        }
      }

      navigation(
          startDestination = NavigationScreens.Chat.route,
          route = NavigationScreens.Chat.name,
      ) {
        composable(NavigationScreens.Chat.route) {
          UniverseBackgroundContainer(bitmap) {
            ChatListScreen(
                userID = authInstance.currentUser!!.uid,
                onTabSelected = onTabSelected,
                onChatSelected = { chatID, chatName ->
                  navController.navigate(
                      route =
                          NavigationScreens.ChatInstance.route
                              .replace("{chatID}", chatID)
                              .replace("{chatName}", chatName))
                })
          }
        }

        composable(
            route = NavigationScreens.ChatInstance.route,
            arguments =
                listOf(
                    navArgument("chatID") { type = NavType.StringType },
                    navArgument("chatName") { type = NavType.StringType })) {
              UniverseBackgroundContainer(bitmap) {
                ChatScreen(
                    chatID = it.arguments?.getString("chatID")!!,
                    chatName = it.arguments?.getString("chatName")!!,
                    userID = authInstance.currentUser!!.uid,
                    onTabSelected = onTabSelected,
                    onBack = { navigationActions.goBack() })
              }
            }
      }

      navigation(
          startDestination = NavigationScreens.Profile.route,
          route = NavigationScreens.Profile.name,
      ) {
        composable(NavigationScreens.Profile.route) {
          UniverseBackgroundContainer(bitmap) {
            UserProfileScreen(
                uid = authInstance.currentUser!!.uid,
                onTabSelected = onTabSelected,
                onEditProfileClick = { uid ->
                  navController.navigate(NavigationScreens.Settings.route.replace("{uid}", uid))
                },
                onChatNavigate = { chatID, chatName ->
                  navController.navigate(
                      route =
                          NavigationScreens.ChatInstance.route
                              .replace("{chatID}", chatID)
                              .replace("{chatName}", chatName)
                              .replace("{userID}", authInstance.currentUser!!.uid))
                },
                onCardClick = { eventId: String, eventLocation: Location ->
                  navController.navigate(
                      NavigationScreens.MapInstance.route
                          .replace("{eventId}", eventId)
                          .replace("{latitude}", eventLocation.latitude.toFloat().toString())
                          .replace("{longitude}", eventLocation.longitude.toFloat().toString()))
                })
          }
        }
      }
      composable(
          route = NavigationScreens.Settings.route,
          arguments = listOf(navArgument("uid") { type = NavType.StringType })) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: "0"
            UniverseBackgroundContainer(bitmap) {
              SettingsScreen(
                  uid = uid,
                  onBack = {
                    navController.popBackStack(NavigationScreens.Profile.route, inclusive = false)
                  },
                  onConfirm = { navigationActions.navigateTo(NavigationScreens.Profile) },
                  onLogout = { navigationActions.navigateTo(NavigationScreens.SignIn) },
                  clear = {
                    credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
                  })
            }
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

              UniverseBackgroundContainer(bitmap) {
                EventCreationScreen(
                    location = Location(latitude.toDouble(), longitude.toDouble()),
                    onSave = {
                      navController.navigate(route = NavigationScreens.SelectTagEvent.route)
                    },
                    onBack = { navigationActions.goBack() })
              }
            }
        composable(
            route = NavigationScreens.SelectTagEvent.route,
        ) {
          UniverseBackgroundContainer(bitmap) {
            SelectTagScreen(
                selectTagMode = SelectTagMode.EVENT_CREATION,
                uid = authInstance.currentUser!!.uid,
                navigateOnSave = { navigationActions.navigateTo(NavigationScreens.Map) },
                onBack = { navigationActions.goBack() })
          }
        }
      }
    }
  }
}
