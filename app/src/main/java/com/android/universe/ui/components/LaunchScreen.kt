package com.android.universe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.android.universe.R
import kotlinx.coroutines.delay

/**
 * Represents the lifecycle of the app launch overlay.
 * - [Loading]: The launch overlay is fully visible.
 * - [Exiting]: The app is ready and the overlay should animate out.
 */
enum class LaunchState {
  Loading,
  Exiting
}

/**
 * Remembers and drives the current [LaunchState].
 *
 * This composable guarantees:
 * - A minimum display duration (for perceived smoothness / premium feel)
 * - A clean transition to [LaunchState.Exiting] once [isReady] becomes true
 *
 * @param isReady Whether the app has completed its startup work
 * @return A stable [State] representing the current [LaunchState]
 */
@Composable
fun rememberLaunchState(isReady: Boolean = true): State<LaunchState> {
  var state by remember { mutableStateOf(LaunchState.Loading) }

  LaunchedEffect(Unit) { delay(1200) }

  LaunchedEffect(isReady) {
    if (isReady) {
      delay(1000)
      state = LaunchState.Exiting
    }
  }

  return remember { derivedStateOf { state } }
}

/**
 * Fullscreen launch overlay displayed during app startup.
 *
 * Automatically fades out when [state] transitions to [LaunchState.Exiting].
 *
 * @param state Current launch state controlling visibility
 */
@Composable
fun LaunchOverlay(state: LaunchState) {
  AnimatedVisibility(
      visible = state != LaunchState.Exiting, exit = fadeOut(animationSpec = tween(700))) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center) {
              LogoWithCircularProgress()
            }
      }
}

/**
 * Displays the app launcher icon centered inside a circular loading indicator.
 *
 * Used exclusively within the launch overlay to reinforce branding during startup.
 */
@Composable
private fun LogoWithCircularProgress() {
  val context = LocalContext.current
  val launcherDrawable = remember { ContextCompat.getDrawable(context, R.mipmap.universe_launcher) }
  val bitmap = remember(launcherDrawable) { launcherDrawable?.toBitmap()?.asImageBitmap() }

  Box(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(modifier = Modifier.size(210.dp), strokeWidth = 8.dp)

    if (bitmap != null) {
      Image(
          bitmap = bitmap,
          contentDescription = "Universe Launcher Icon",
          modifier = Modifier.size(160.dp))
    }
  }
}
