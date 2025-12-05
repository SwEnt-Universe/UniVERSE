package com.android.universe.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** A generic factory helper to reduce boilerplate for ViewModels with arguments. */
inline fun <VM : ViewModel> viewModelFactory(
    crossinline initializer: () -> VM
): ViewModelProvider.Factory {
  return object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return initializer() as T
    }
  }
}
