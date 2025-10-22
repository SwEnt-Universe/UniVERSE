package com.android.universe.ui.theme

import androidx.compose.ui.unit.dp

object Dimensions {
  // Paddings (consolidating SettingsScreenPaddings, TagGroup, SelectTagScreen)
  val PaddingSmall = 4.dp // InternalSpacing, tag padding
  val PaddingMedium = 8.dp // General spacing, tag group horizontal
  val PaddingLarge = 16.dp // Section titles, content padding
  val PaddingExtraLarge = 20.dp // ContentHorizontalPadding, DividerPadding
  val PaddingErrorIndent = 8.dp // ErrorIndent
  val PaddingFieldIconSpacing = 10.dp // FieldIconSpacing
  val PaddingDateFieldSpacing = 8.dp // DateFieldSpacing

  // Icon sizes (from TagGroup, SelectTagScreen)
  val IconSizeSmall = 16.dp // Delete icon
  val IconSizeMedium = 18.dp // Check icon in tags
  val IconSizeLarge = 36.dp // General icons (e.g., Edit, ArrowBack)

  // Divider
  val DividerThickness = 0.5.dp // SettingsScreen divider
  val DividerThick = 1.dp // SelectTagScreen divider

  // Border
  val BorderWidth = 2.dp // Selected tag border

  // Elevation
  val ElevationCard = 4.dp

  // Corner radii
  val CornerRadiusButton = 8.dp // Tag buttons

  // Spacer sizes
  val SpacerSmall = 4.dp // Between text and icons in tags
  val SpacerMedium = 8.dp // Between fields
}
