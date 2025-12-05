package com.android.universe

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.core.view.WindowCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.universe.model.location.Location
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.resources.C
import com.android.universe.ui.chat.ChatListScreen
import com.android.universe.ui.chat.ChatScreen
import com.android.universe.ui.common.UniverseBackgroundContainer
import com.android.universe.ui.emailVerification.EmailVerificationScreen
import com.android.universe.ui.event.EventScreen
import com.android.universe.ui.eventCreation.EventCreationScreen
import com.android.universe.ui.eventCreation.EventCreationViewModel
import com.android.universe.ui.map.MapMode
import com.android.universe.ui.map.MapScreen
import com.android.universe.ui.map.MapViewModel
import com.android.universe.ui.map.MapViewModelFactory
import com.android.universe.ui.navigation.NavigationActions
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.navigation.resolveUserDestinationScreen
import com.android.universe.ui.profile.UserProfileScreen
import com.android.universe.ui.profileCreation.AddProfile
import com.android.universe.ui.profileCreation.AddProfileViewModel
import com.android.universe.ui.profileSettings.SettingsScreen
import com.android.universe.ui.profileSettings.SettingsViewModel
import com.android.universe.ui.selectTag.SelectTagMode
import com.android.universe.ui.selectTag.SelectTagScreen
import com.android.universe.ui.signIn.SignInScreen
import com.android.universe.ui.theme.UniverseTheme
import com.android.universe.ui.utils.LocalLayerBackdrop
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.tomtom.sdk.featuretoggle.FeatureToggleController
import com.tomtom.sdk.featuretoggle.TomTomOrbisMapFeature
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val mapViewModel by viewModels<MapViewModel> { MapViewModelFactory(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    // Enable edge-to-edge with auto-contrast (icons adapt to content/theme)
    enableEdgeToEdge()
    FeatureToggleController.enable(TomTomOrbisMapFeature)
    // Enable automatic index creation for Firestore (makes offline queries faster)
    Firebase.firestore.persistentCacheIndexManager?.apply { enableIndexAutoCreation() }

    setContent {
      UniverseTheme {
        val backgroundColor = Color.Transparent
        val backdrop = rememberLayerBackdrop {
          drawRect(backgroundColor)
          drawContent()
        }
        CompositionLocalProvider(LocalLayerBackdrop provides backdrop) {
          Surface(
              modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
              color = Color.Transparent) {
                UniverseBackgroundContainer(mapViewModel = mapViewModel) {
                  UniverseApp(mapViewModel = mapViewModel)
                }
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
@SuppressLint("LocalContextResourcesRead")
@Composable
fun UniverseApp(
    context: Context = LocalContext.current,
    mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(context)),
    credentialManager: CredentialManager = CredentialManager.create(context),
) {
  val authInstance = remember { FirebaseAuth.getInstance() }
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val userRepository = UserRepositoryProvider.repository
  val mainActivityScope = rememberCoroutineScope()

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
          var navigateToDestination by remember { mutableStateOf<NavigationScreens?>(null) }

          LaunchedEffect(navigateToDestination) {
            navigateToDestination?.let { destination -> navigationActions.navigateTo(destination) }
          }
          SignInScreen(
              onSignedIn = {
                mainActivityScope.launch {
                  navigateToDestination = resolveUserDestinationScreen(userRepository)
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
          val vm: AddProfileViewModel =
              viewModel(factory = AddProfileViewModel.provideFactory(context))

          AddProfile(
              uid = authInstance.currentUser!!.uid,
              navigateOnSave = { navigationActions.navigateTo(NavigationScreens.SelectTagUser) },
              onBack = {
                // Navigate back to Sign In
                navController.navigate(NavigationScreens.SignIn.route) {
                  popUpTo(NavigationScreens.AddProfile.route) { inclusive = true }
                }
              },
              viewModel = vm)
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

      navigation(
          route = NavigationScreens.SelectTagUser.name,
          startDestination = NavigationScreens.SelectTagUser.route) {
            composable(NavigationScreens.SelectTagUser.route) {
              SelectTagScreen(
                  uid = authInstance.currentUser!!.uid,
                  navigateOnSave = { navigationActions.navigateTo(NavigationScreens.Map) },
                  onBack = { navigationActions.goBack() })
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
              onNavigateToEventCreation = {
                navController.navigate(NavigationScreens.EventCreation.route)
              },
              onChatNavigate = { chatID, chatName ->
                navController.navigate(
                    route =
                        NavigationScreens.ChatInstance.route
                            .replace("{chatID}", chatID)
                            .replace("{chatName}", chatName)
                            .replace("{userID}", authInstance.currentUser!!.uid))
              },
              viewModel = mapViewModel)
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
                  onNavigateToEventCreation = {
                    navController.navigate(NavigationScreens.EventCreation.route)
                  },
                  preselectedEventId = eventId,
                  preselectedLocation = Location(lat, lng),
                  onChatNavigate = { chatID, chatName ->
                    navController.navigate(
                        NavigationScreens.ChatInstance.route
                            .replace("{chatID}", chatID)
                            .replace("{chatName}", chatName)
                            .replace("{userID}", authInstance.currentUser!!.uid))
                  },
                  viewModel = mapViewModel)
            }

        navigation(
            startDestination = NavigationScreens.Event.route,
            route = NavigationScreens.Event.name,
        ) {
          composable(NavigationScreens.Event.route) {
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

        navigation(
            startDestination = NavigationScreens.Chat.route,
            route = NavigationScreens.Chat.name,
        ) {
          composable(NavigationScreens.Chat.route) {
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

          composable(
              route = NavigationScreens.ChatInstance.route,
              arguments =
                  listOf(
                      navArgument("chatID") { type = NavType.StringType },
                      navArgument("chatName") { type = NavType.StringType })) {
                ChatScreen(
                    chatID = it.arguments?.getString("chatID")!!,
                    chatName = it.arguments?.getString("chatName")!!,
                    userID = authInstance.currentUser!!.uid,
                    onTabSelected = onTabSelected,
                    onBack = { navigationActions.goBack() })
              }
        }

        navigation(
            startDestination = NavigationScreens.Profile.route,
            route = NavigationScreens.Profile.name,
        ) {
          composable(NavigationScreens.Profile.route) {
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
        composable(
            route = NavigationScreens.Settings.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })) { backStackEntry
              ->
              val uid = backStackEntry.arguments?.getString("uid") ?: "0"
              val vm: SettingsViewModel =
                  viewModel(factory = SettingsViewModel.provideFactory(context, uid))
              SettingsScreen(
                  uid = uid,
                  viewModel = vm,
                  onBack = {
                    navController.popBackStack(NavigationScreens.Profile.route, inclusive = false)
                  },
                  onConfirm = { navigationActions.navigateTo(NavigationScreens.Profile) },
                  onLogout = { navigationActions.navigateTo(NavigationScreens.SignIn) },
                  onAddTag = {
                    navController.navigate(NavigationScreens.SelectTagUserSettings.route)
                  },
                  clear = {
                    credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
                  })
            }
        navigation(
            route = NavigationScreens.EventCreation.name,
            startDestination = NavigationScreens.EventCreation.route) {

              // --- Main Event Creation Screen ---
              composable(NavigationScreens.EventCreation.route) { entry ->
                val vm: EventCreationViewModel =
                    viewModel(
                        viewModelStoreOwner = entry,
                        factory = EventCreationViewModel.provideFactory(context))

                EventCreationScreen(
                    eventCreationViewModel = vm,
                    onSelectLocation = {
                      navController.navigate(NavigationScreens.SelectLocation.route)
                    },
                    onSave = { navController.navigate(NavigationScreens.SelectTagEvent.route) },
                    onBack = { navigationActions.goBack() })
              }

              // --- Location Picker Screen ---
              composable(NavigationScreens.SelectLocation.route) { backStackEntry ->
                // IMPORTANT: Share parent VM
                val parentEntry =
                    remember(backStackEntry) {
                      navController.getBackStackEntry(NavigationScreens.EventCreation.route)
                    }

                val vm: EventCreationViewModel =
                    viewModel(
                        viewModelStoreOwner = parentEntry,
                        factory = EventCreationViewModel.provideFactory(context))
                mapViewModel.setMapMode(MapMode.SELECT_LOCATION)

                MapScreen(
                    uid = authInstance.currentUser!!.uid,
                    onTabSelected = {},
                    onNavigateToEventCreation = {
                      navController.navigate(NavigationScreens.EventCreation.route)
                    },
                    onLocationSelected = { lat, lon ->
                      vm.setLocation(lat, lon)
                      navController.popBackStack()
                    },
                    viewModel = mapViewModel)
              }

              // --- Add Tags Screen FOR EVENT---
              composable(NavigationScreens.SelectTagEvent.route) {
                SelectTagScreen(
                    selectTagMode = SelectTagMode.EVENT_CREATION,
                    uid = authInstance.currentUser!!.uid,
                    navigateOnSave = {
                      navController.popBackStack(
                          route = NavigationScreens.Map.route, inclusive = false)
                    },
                    onBack = { navigationActions.goBack() })
              }
            }
        // --- Add Tags Screen FOR USER---
        composable(
            route = NavigationScreens.SelectTagUserSettings.route,
        ) {
          SelectTagScreen(
              selectTagMode = SelectTagMode.SETTINGS,
              uid = authInstance.currentUser!!.uid,
              navigateOnSave = { navigationActions.goBack() },
              onBack = { navigationActions.goBack() })
        }
      }
    }
  }
}
