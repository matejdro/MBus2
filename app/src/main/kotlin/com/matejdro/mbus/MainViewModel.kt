package com.matejdro.mbus

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.android.Location
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainViewModel @AssistedInject constructor(
   @Assisted
   startIntent: Intent,
) : ViewModel() {
   private val _startingScreen = MutableStateFlow<ScreenKey?>(null)
   val startingScreen: StateFlow<ScreenKey?> = _startingScreen

   init {
      viewModelScope.launch {
         val forcedLocation = if (startIntent.hasExtra(EXTRA_FORCED_LAT) && startIntent.hasExtra(EXTRA_FORCED_LON)) {
            val forcedLat = startIntent.getDoubleExtra(EXTRA_FORCED_LAT, 0.0)
            val forcedLon = startIntent.getDoubleExtra(EXTRA_FORCED_LON, 0.0)
            Location(forcedLat, forcedLon)
         } else {
            null
         }

         _startingScreen.value = HomeMapScreenKey(forcedLocation)
      }
   }

   @AssistedFactory
   interface Factory {
      fun create(intent: Intent): MainViewModel
   }
}

private const val EXTRA_FORCED_LAT = "benchmark_forced_lat"
private const val EXTRA_FORCED_LON = "benchmark_forced_lon"
