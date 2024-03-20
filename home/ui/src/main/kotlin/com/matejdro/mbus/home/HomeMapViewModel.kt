package com.matejdro.mbus.home

import androidx.compose.runtime.Stable
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import com.matejdro.mbus.stops.StopsRepository
import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

   override fun onServiceRegistered() {
      loadStops()
   }

   private fun loadStops() = resources.launchResourceControlTask(_stops) {
      emitAll(stopsRepository.getAllStops())
   }
}
