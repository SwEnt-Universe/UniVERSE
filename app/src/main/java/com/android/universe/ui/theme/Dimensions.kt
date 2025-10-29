package com.android.universe.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized design constants for spacing, sizing, borders, and elevation across the **UniVERSE**
 * app.
 *
 * These values unify margins, paddings, icon sizes, border widths, and corner radii
 *
 * ### Design Philosophy
 * - Promotes **visual consistency** across screens.
 * - Encourages **scalable** and **readable** UI definitions by avoiding magic numbers.
 * - Enables **responsive refactoring** by defining spacing constants in one place.
 *
 * ### Usage Example
 *
 * ```
 * Column(
 *     modifier = Modifier.padding(Dimensions.PaddingLarge)
 * ) {
 *     Text("Settings")
 *     Spacer(Modifier.height(Dimensions.SpacerMedium))
 *     Divider(thickness = Dimensions.DividerThickness)
 * }
 * ```
 */
object Dimensions {
  // Padding sizes
  val PaddingSmall = 4.dp // InternalSpacing, tag padding
  val PaddingMedium = 8.dp // General spacing, tag group horizontal
  val PaddingLarge = 16.dp // Section titles, content padding
  val PaddingExtraLarge = 24.dp // ContentHorizontalPadding, DividerPadding
  val PaddingErrorIndent = 8.dp // ErrorIndent
  val PaddingFieldIconSpacing = 10.dp // FieldIconSpacing

  // Spacer sizes
  val SpacerSmall = 4.dp // InternalSpacing, tag padding
  val SpacerMedium = 8.dp // General spacing, tag group horizontal
  val SpacerLarge = 16.dp // Section titles, content padding
  val SpacerExtraLarge = 24.dp // ContentHorizontalPadding, DividerPadding

  // Icon sizes (from TagGroup, SelectTagScreen)
  val IconSizeSmall = 16.dp // Delete icon
  val IconSizeMedium = 18.dp // Check icon in tags
  val IconSizeLarge = 36.dp // General icons (e.g., Edit, ArrowBack)

  // Divider
  val DividerThickness = 0.5.dp // SettingsScreen divider

  // Border
  val BorderWidth = 2.dp // Selected tag border

  // Elevation
  val ElevationCard = 4.dp

  // Corner radii
  val RoundedCorner = 8.dp // General rounded corners
}
