package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome

class FakeStopsRepository : StopsRepository {
   private var providedStops = MutableStateFlow<Outcome<List<Stop>>?>(null)

   fun provideStops(stops: Outcome<List<Stop>>) {
      providedStops.value = stops
   }

   override fun getAllStops(): Flow<Outcome<List<Stop>>> {
      return providedStops.map { it ?: error("fake stops not provided") }
   }
}
