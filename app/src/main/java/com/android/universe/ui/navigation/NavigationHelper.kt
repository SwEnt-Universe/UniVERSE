package com.android.universe.ui.navigation

import com.android.universe.model.user.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking

/**
 * Determines the appropriate initial screen for the user based on their authentication state and
 * profile status.
 *
 * This function follows a specific logic flow:
 * 1. If the user is not authenticated (`user` is null), it directs them to the
 *    [NavigationScreens.SignIn] screen.
 * 2. If the user is authenticated: a. If they are an anonymous user, they are sent to the main
 *    [NavigationScreens.Map] screen. b. If they are a non-anonymous user and already have a profile
 *    in the `userRepository`, they are also sent to the [NavigationScreens.Map] screen. c. If they
 *    are a non-anonymous user but do not have a profile, they are directed to the
 *    [NavigationScreens.AddProfile] screen to create one.
 *
 * @param userRepository The repository to check for the user's profile.
 * @param user The current Firebase user. Defaults to `Firebase.auth.currentUser`.
 * @return The [NavigationScreens] destination that the user should be navigated to.
 */
suspend fun resolveUserDestinationScreen(
    userRepository: UserRepository,
    user: FirebaseUser? = Firebase.auth.currentUser
): NavigationScreens =
    when {
      user == null -> NavigationScreens.SignIn
      user.isAnonymous || userRepository.hasProfile(user.uid) -> NavigationScreens.Map
      else -> NavigationScreens.AddProfile
    }

/**
 * Checks if a user profile exists for the given UID.
 *
 * @param uid The unique identifier of the user to check.
 * @return `true` if the user profile is found, `false` otherwise.
 */
private suspend fun UserRepository.hasProfile(uid: String): Boolean = runCatching {
  getUser(uid) }.isSuccess

