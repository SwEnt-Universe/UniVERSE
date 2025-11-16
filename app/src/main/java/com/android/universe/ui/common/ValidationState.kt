package com.android.universe.ui.common

/**
 * Represents the result of a validation check and UI state. This is used by ViewModels (for logic)
 * and Composables (for display).
 */
sealed class ValidationState {
  /** Indicates that the input is valid. */
  data object Valid : ValidationState()

  /** Indicates that the input is untouched or doesn't require validation. */
  data object Neutral : ValidationState()

  /**
   * Indicates that the input is invalid.
   *
   * @property errorMessage A descriptive message explaining the validation failure.
   */
  data class Invalid(val errorMessage: String) : ValidationState()
}
