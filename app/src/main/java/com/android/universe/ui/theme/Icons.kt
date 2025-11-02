package com.android.universe.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized icon definitions for the UniVERSE design system.
 *
 * Each icon represents a semantic purpose (logout, settings, etc.), not a specific drawable
 * resource — this allows easy swapping between filled / outlined / custom sets for different
 * themes.
 */
data class UniverseIcons(val logout: ImageVector)

// --- Light theme icon set ---
val LightIcons = UniverseIcons(logout = Icons.AutoMirrored.Outlined.Logout)

// --- Dark theme icon set ---
val DarkIcons = UniverseIcons(logout = Icons.AutoMirrored.Filled.Logout)

// CompositionLocal to provide icons throughout the UI
val LocalUniverseIcons = staticCompositionLocalOf { LightIcons }
