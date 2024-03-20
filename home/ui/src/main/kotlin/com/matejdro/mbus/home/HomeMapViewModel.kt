package com.matejdro.mbus.home

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import com.matejdro.mbus.stops.StopsRepository
import com.matejdro.mbus.stops.model.Stop
import dispatch.core.dispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import javax.inject.Inject

@Stable
class HomeMapViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val stopsRepository: StopsRepository,
) : SingleScreenViewModel<HomeMapScreenKey>(resources.scope) {
   private val _stops = MutableStateFlow<Outcome<List<Stop>>>(Outcome.Progress())
   val stops: StateFlow<Outcome<List<Stop>>> = _stops

   private var previousBounds: LatLngBounds? = null

   private val defaultDispatcher = resources.scope.coroutineContext.dispatcherProvider.default

   fun loadStops(newBounds: LatLngBounds) {
      val halfWidth = newBounds.northeast.longitude - newBounds.center.longitude
      if (halfWidth > MAX_HALF_WIDTH_TO_SHOW_POINTS) {
         resources.cancelResource(_stops)
         _stops.value = Outcome.Success(emptyList())
         previousBounds = null
         return
      }

      val previousBounds = previousBounds
      if (previousBounds != null && previousBounds.contains(newBounds)) {
         return
      }

      resources.launchResourceControlTask(_stops, context = defaultDispatcher) {
         val expandedBounds = newBounds.expandByFiftyPercent()

         emitAll(
            stopsRepository.getAllStopsWithinRect(
               minLat = expandedBounds.southwest.latitude,
               maxLat = expandedBounds.northeast.latitude,
               minLon = expandedBounds.southwest.longitude,
               maxLon = expandedBounds.northeast.longitude,
            ).onEach {
               this@HomeMapViewModel.previousBounds = expandedBounds
            }
         )
      }
   }

   private fun LatLngBounds.expandByFiftyPercent(): LatLngBounds {
      val center = center
      val halfHeight = northeast.latitude - center.latitude
      val halfWidth = northeast.longitude - center.longitude
      val expandedHalfHeight = halfHeight * MAP_RELOAD_THRESHOLD
      val expandedHalfWidth = halfWidth * MAP_RELOAD_THRESHOLD

      return LatLngBounds(
         LatLng(center.latitude - expandedHalfHeight, center.longitude - expandedHalfWidth),
         LatLng(center.latitude + expandedHalfHeight, center.longitude + expandedHalfWidth),
      )
   }
}

private fun LatLngBounds.contains(other: LatLngBounds): Boolean {
   return contains(other.northeast) && contains(other.southwest)
}

private const val MAP_RELOAD_THRESHOLD = 1.5
private const val MAX_HALF_WIDTH_TO_SHOW_POINTS = 0.01
