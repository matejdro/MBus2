package com.matejdro.mbus.stops

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.stops.model.toStop
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class StopsRepositoryImpl @Inject constructor(
   private val stopsService: StopsService,
) : StopsRepository {
   override suspend fun getAllStops(): List<Stop> {
      return stopsService.getAllStops().stops.mapNotNull {
         it.toStop()
      }
   }
}
