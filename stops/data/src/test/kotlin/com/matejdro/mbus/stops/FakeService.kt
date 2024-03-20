package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stops

class FakeService : StopsService {
   var providedStops: Stops? = null
   override suspend fun getAllStops(): Stops {
      return providedStops ?: error("fake stops not provided")
   }
}
