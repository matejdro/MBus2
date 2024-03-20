package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop

interface StopsRepository {
   suspend fun getAllStops(): List<Stop>
}
