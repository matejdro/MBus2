package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stops
import si.inova.kotlinova.retrofit.FakeService
import si.inova.kotlinova.retrofit.ServiceTestingHelper

class FakeStopsService(private val helper: ServiceTestingHelper = ServiceTestingHelper()) : StopsService, FakeService by helper {
   var providedStops: Stops? = null
   var numLoads: Int = 0

   override suspend fun getAllStops(): Stops {
      helper.intercept()

      numLoads++
      return providedStops ?: error("fake stops not provided")
   }
}
