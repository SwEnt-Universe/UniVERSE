package com.android.universe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Expose current darkTheme state from UniverseTheme
val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Immutable
data class ExtendedColors(
    val success: Color,
    val placeholder: Color,
    val toggleActive: Color,
    val toggleTrack: Color,
    val overImage: Color,
)

/**
 * Provides access to the [ExtendedColors] for the current theme.
 *
 * We use `staticCompositionLocalOf` because the theme is unlikely to change (light/dark mode)
 * during composition in a way that would require invalidating *every* composable. This is more
 * efficient.
 *
 * The `Color.Unspecified` default is a "safe" default that ensures any composable accidentally
 * reading this *without* a `UniverseTheme` provider above it will likely crash, revealing the bug.
 */
val LocalExtendedColors = staticCompositionLocalOf {
  ExtendedColors(
      success = Color.Unspecified,
      placeholder = Color.Unspecified,
      toggleActive = Color.Unspecified,
      toggleTrack = Color.Unspecified,
      overImage = Color.Unspecified)
}

/**
 * This composable wraps the given [content] in a [MaterialTheme] configuration, providing colors,
 * typography, and surface defaults consistent with UniVERSE’s design language.
 *
 * ### Behavior
 * - Chooses light or dark color schemes automatically based on [darkTheme], or system theme when
 *   not explicitly specified.
 * - Optionally uses **dynamic color schemes** on Android 12+ devices when [dynamicColor] is true.
 *   (Dynamic colors override the app’s custom palette using system-generated tones.)
 *
 * ### Parameters
 *
 * @param darkTheme Whether to enable the dark color scheme. Defaults to the system theme via
 *   [isSystemInDarkTheme].
 * @param dynamicColor Enables Material You dynamic color on supported devices (Android 12+). When
 *   true, custom UniVERSE colors are overridden by system-derived tones.
 * @param content The composable to which the UniVERSE theme is applied.
 *
 * ### Notice that it can be applied to previews as well
 *
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
  val extendedColors =
      if (darkTheme) {
        ExtendedColors(
            success = SuccessDark,
            placeholder = PlaceholderDark,
            toggleActive = ToggleActiveDark,
            toggleTrack = ToggleTrackDark,
            overImage = overImageDark)
      } else {
        ExtendedColors(
            success = SuccessLight,
            placeholder = PlaceholderLight,
            toggleActive = ToggleActiveLight,
            toggleTrack = ToggleTrackLight,
            overImage = overImageLight)
      }
  CompositionLocalProvider(
      LocalIsDarkTheme provides darkTheme,
      LocalUniverseIcons provides if (darkTheme) DarkIcons else LightIcons,
      LocalExtendedColors provides extendedColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
      }
}

/**
 * Determines the display color for a tag based on its category and selection state.
 *
 * This function returns a vibrant category-specific color when the tag is unselected, and a
 * lighter, more transparent version of that color when selected (simulating a "pressed" or "glassy"
 * state).
 *
 * The colors are predefined for each category:
 * - Music: Violet
 * - Sport: Sky Blue
 * - Food: Yellow
 * - Art: Red
 * - Travel: Brown
 * - Games: Orange
 * - Technology: Grey
 * - Topic: Pink
 *
 * @param category The string representation of the tag's category (e.g., "Music", "Sport").
 * @param isSelected Whether the tag is currently selected. If true, the lighter color variant is
 *   returned.
 * @return The [Color] corresponding to the category and state. Defaults to
 *   `MaterialTheme.colorScheme.primary` if the category is not recognized.
 */
@Composable
fun tagColor(category: String, isSelected: Boolean = false): Color {
  val isDark = UniverseTheme.isDark
  return when (category) {
    "Music" ->
        if (isSelected) (if (isDark) TagMusicSelectedDark else TagMusicSelected) else TagMusic
    "Sport" ->
        if (isSelected) (if (isDark) TagSportSelectedDark else TagSportSelected) else TagSport
    "Food" -> if (isSelected) (if (isDark) TagFoodSelectedDark else TagFoodSelected) else TagFood
    "Art" -> if (isSelected) (if (isDark) TagArtSelectedDark else TagArtSelected) else TagArt
    "Travel" ->
        if (isSelected) (if (isDark) TagTravelSelectedDark else TagTravelSelected) else TagTravel
    "Games" ->
        if (isSelected) (if (isDark) TagGamesSelectedDark else TagGamesSelected) else TagGames
    "Technology" ->
        if (isSelected) (if (isDark) TagTechnologySelectedDark else TagTechnologySelected)
        else TagTechnology
    "Topic" ->
        if (isSelected) (if (isDark) TagTopicSelectedDark else TagTopicSelected) else TagTopic
    else -> MaterialTheme.colorScheme.primary
  }
}

object UniverseTheme {
  val icons: UniverseIcons
    @Composable get() = LocalUniverseIcons.current

  val isDark: Boolean
    @Composable get() = LocalIsDarkTheme.current

  val extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current
}
