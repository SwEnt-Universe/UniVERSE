package com.android.universe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * This composable wraps the given [content] in a [MaterialTheme] configuration, providing
 * colors, typography, and surface defaults consistent with UniVERSE’s design language.
 *
 * ### Behavior
 * - Chooses **light** or **dark** color schemes automatically based on [darkTheme],
 *   or system theme when not explicitly specified.
 * - Optionally uses **dynamic color schemes** on Android 12+ devices when [dynamicColor] is true.
 *   (Dynamic colors override the app’s custom palette using system-generated tones.)
 *
 * ### Parameters
 * @param darkTheme Whether to enable the dark color scheme.
 * Defaults to the system theme via [isSystemInDarkTheme].
 *
 * @param dynamicColor Enables Material You dynamic color extraction on supported devices
 * (Android 12+). When true, custom UniVERSE colors are overridden by system-derived tones.
 *
 * @param content The composable UI content to which the UniVERSE theme is applied.
 *
 * ### Notice that it can be applied to previews as well
 * ```
 * UniverseTheme {
 *     thingToPreview {
 *         Text("Hello Universe")
 *     }
 * }
 * ```
 */
@Composable
fun UniverseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // Setting this to true overrides custom colors with dynamic colors (?) on supported devices
    // Can be explored, but for now we keep it false to use our custom color schemes
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
  val colorScheme =
      when {
        dynamicColor -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme ->
            darkColorScheme(
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark, // cards, sheets, (things that go on top of background)
                onSurface = OnSurfaceDark, // things that go on top of surface
                primary = PrimaryDark,
                onPrimary = OnPrimaryDark,
                secondary = SecondaryDark,
                onSecondary = OnSecondaryDark,
                error = ErrorDark,
                onError = OnErrorDark)
        else ->
            lightColorScheme(
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight, // cards, sheets, (things that go on top of background)
                onSurface = OnSurfaceLight, // things that go on top of surface
                primary = PrimaryLight,
                onPrimary = OnPrimaryLight,
                secondary = SecondaryLight,
                onSecondary = OnSecondaryLight,
                error = ErrorLight,
                onError = OnErrorLight)
      }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Extension to access tag colors dynamically by category
@Composable
fun tagColor(
    category: String,
    isSelected: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme()
): Color {
  return when (category) {
    "INTEREST" ->
        if (isSelected) TagSelectedDark else if (darkTheme) TagInterestDark else TagInterestLight
    "SPORT" -> if (isSelected) TagSelectedDark else if (darkTheme) TagSportDark else TagSportLight
    "MUSIC" -> if (isSelected) TagSelectedDark else if (darkTheme) TagMusicDark else TagMusicLight
    "TRANSPORT" ->
        if (isSelected) TagSelectedDark else if (darkTheme) TagTransportDark else TagTransportLight
    "CANTON" ->
        if (isSelected) TagSelectedDark else if (darkTheme) TagCantonDark else TagCantonLight
    else -> MaterialTheme.colorScheme.primary
  }
}
