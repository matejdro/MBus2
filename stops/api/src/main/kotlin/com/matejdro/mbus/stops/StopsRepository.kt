package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome
import java.time.Instant

interface StopsRepository {
   fun getAllStops(): Flow<Outcome<List<Stop>>>
   fun getAllStopsWithinRect(
      minLat: Double,
      maxLat: Double,
      minLon: Double,
      maxLon: Double,
   ): Flow<Outcome<List<Stop>>>

   suspend fun getLastStopUpdate(id: Long): Instant?
   suspend fun setLastStopUpdate(id: Long, updateTime: Instant)
}
