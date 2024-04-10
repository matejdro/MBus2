package com.matejdro.mbus.stops.model

import java.time.Instant

data class Stop(
   val id: Int,
   val name: String,
   val lat: Double,
   val lon: Double,
   val description: String? = null,
   val imageUrl: String? = null,
   val lastScheduleUpdate: Instant? = null,
   val whitelistedLines: Set<Int> = emptySet(),
)
