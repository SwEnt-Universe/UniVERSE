package com.android.universe.model.map

/** Represents the different UI states for the Map Screen. */
sealed class MapUiState {
    data object Idle : MapUiState()

    data object PermissionRequired : MapUiState()

    data object LocationUnavailable : MapUiState()

    data object LocationAvailable : MapUiState()

    data object Tracking : MapUiState()

    data class Error(val message: String) : MapUiState()
}
