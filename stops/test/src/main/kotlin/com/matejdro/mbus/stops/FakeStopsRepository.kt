package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop

class FakeStopsRepository : StopsRepository {
   var providedStops: List<Stop>? = null

   override suspend fun getAllStops(): List<Stop> {
      return providedStops ?: error("stops not provided")
   }
}
