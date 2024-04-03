package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface StopsRepository {
   fun getAllStops(): Flow<Outcome<List<Stop>>>
   fun getAllStopsWithinRect(
      minLat: Double,
      maxLat: Double,
      minLon: Double,
      maxLon: Double,
   ): Flow<Outcome<List<Stop>>>

   suspend fun update(stop: Stop)
}
