package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stops
import retrofit2.http.GET

interface StopsService {
   @GET("GetAllStops")
   suspend fun getAllStops(): Stops
}
