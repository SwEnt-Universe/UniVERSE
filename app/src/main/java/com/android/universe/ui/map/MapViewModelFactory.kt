package com.android.universe.ui.map

import android.content.Context
import android.content.res.Configuration
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
          applicationContext = context.applicationContext,
          prefs = context.getSharedPreferences("map_pref", Context.MODE_PRIVATE),
          locationRepository = TomTomLocationRepository(context),
          eventRepository = EventRepositoryProvider.repository,
          userRepository = UserRepositoryProvider.repository,
          isDarkTheme =
              context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                  Configuration.UI_MODE_NIGHT_YES)
          as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
