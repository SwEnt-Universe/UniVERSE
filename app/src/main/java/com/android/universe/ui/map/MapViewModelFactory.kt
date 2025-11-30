package com.android.universe.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.universe.model.event.EventRepositoryProvider
import com.android.universe.model.location.TomTomLocationRepository
import com.android.universe.model.user.UserRepositoryProvider

class MapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return MapViewModel(
          // HERE IS HOW YOU GET IT:
          prefs = context.getSharedPreferences("map_pref", Context.MODE_PRIVATE),
          locationRepository = TomTomLocationRepository(context),
          eventRepository = EventRepositoryProvider.repository,
          userRepository = UserRepositoryProvider.repository)
          as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
