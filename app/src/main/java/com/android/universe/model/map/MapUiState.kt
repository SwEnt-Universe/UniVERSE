package com.android.universe.model.map

import com.android.universe.model.location.Location

/** Represents the different UI states for the Map Screen. */
sealed class MapUiState {
  /** Default idle state when nothing special is happening. */
  data object Idle : MapUiState()

  /** Used when waiting for a location result (initial load or retry). */
  data object Loading : MapUiState()

  /** Shown when the user hasn’t granted location permission. */
  data object PermissionRequired : MapUiState()

  /** When tracking user location actively. */
  data object Tracking : MapUiState()

  /** A successful state that includes a valid location. */
  data class Success(val location: Location) : MapUiState()

  /** When an error occurs (e.g., location failure). */
  data class Error(val message: String) : MapUiState()
}
