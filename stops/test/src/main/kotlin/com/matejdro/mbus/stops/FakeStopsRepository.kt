package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData

class FakeStopsRepository : StopsRepository {
   private var providedStops = MutableStateFlow<Outcome<List<Stop>>?>(null)
   var numLoads = 0

   fun provideStops(stops: Outcome<List<Stop>>) {
      providedStops.value = stops
   }

   override fun getAllStops(): Flow<Outcome<List<Stop>>> {
      numLoads++
      return providedStops.map { it ?: error("fake stops not provided") }
   }

   override fun getAllStopsWithinRect(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<Outcome<List<Stop>>> {
      numLoads++
      return providedStops.map { outcome ->
         val nonNullOutcome = outcome ?: error("fake stops not provided")
         nonNullOutcome.mapData { list ->
            list.filter {
               it.lat >= minLat && it.lat <= maxLat && it.lon >= minLon && it.lon <= maxLon
            }
         }
      }
   }

   override suspend fun update(stop: Stop) {
      providedStops.update { outcome ->
         outcome?.mapData { list ->
            list.map {
               if (it.id == stop.id) {
                  stop
               } else {
                  it
               }
            }
         }
      }
   }

   override suspend fun getStop(id: Int): Stop? {
      val providedStops = providedStops.value ?: error("fake stops not provided")

      return providedStops.data?.firstOrNull { it.id == id }
   }
}
