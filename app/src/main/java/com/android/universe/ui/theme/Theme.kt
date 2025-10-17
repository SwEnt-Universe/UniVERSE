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

@Composable
fun UniverseTheme(
  darkTheme: Boolean = isSystemInDarkTheme(), // From androidx.compose.foundation.isSystemInDarkTheme
  dynamicColor: Boolean = true, // Support Android 12+ dynamic colors
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> darkColorScheme(
      primary = PrimaryDark,
      primaryContainer = PrimaryVariantDark,
      secondary = SecondaryDark,
      secondaryContainer = SecondaryVariantDark,
      background = BackgroundDark,
      surface = SurfaceDark,
      error = ErrorDark,
      onPrimary = OnPrimaryDark,
      onSecondary = OnSecondaryDark,
      onBackground = OnBackgroundDark,
      onSurface = OnSurfaceDark,
      onError = OnErrorDark
    )
    else -> lightColorScheme(
      primary = PrimaryLight,
      primaryContainer = PrimaryVariantLight,
      secondary = SecondaryLight,
      secondaryContainer = SecondaryVariantLight,
      background = BackgroundLight,
      surface = SurfaceLight,
      error = ErrorLight,
      onPrimary = OnPrimaryLight,
      onSecondary = OnSecondaryLight,
      onBackground = OnBackgroundLight,
      onSurface = OnSurfaceLight,
      onError = OnErrorLight
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

// Extension to access tag colors dynamically by category
@Composable
fun tagColor(category: String, isSelected: Boolean = false, darkTheme: Boolean = isSystemInDarkTheme()): Color {
  return when (category) {
    "INTEREST" -> if (isSelected) TagSelectedDark else if (darkTheme) TagInterestDark else TagInterestLight
    "SPORT" -> if (isSelected) TagSelectedDark else if (darkTheme) TagSportDark else TagSportLight
    "MUSIC" -> if (isSelected) TagSelectedDark else if (darkTheme) TagMusicDark else TagMusicLight
    "TRANSPORT" -> if (isSelected) TagSelectedDark else if (darkTheme) TagTransportDark else TagTransportLight
    "CANTON" -> if (isSelected) TagSelectedDark else if (darkTheme) TagCantonDark else TagCantonLight
    else -> MaterialTheme.colorScheme.primary
  }
}