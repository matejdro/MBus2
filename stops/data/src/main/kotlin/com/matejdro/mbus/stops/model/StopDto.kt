package com.matejdro.mbus.stops.model

import com.matejdro.mbus.sqldelight.generated.DbStop
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StopDto(
   @Json(name = "StopPointId")
   val id: Int,
   @Json(name = "Name")
   val name: String,
   @Json(name = "Lat")
   val lat: Double?,
   @Json(name = "Lon")
   val lon: Double?,
)

fun StopDto.toDbStop(): DbStop? {
   if (lat == null || lon == null) return null

   return DbStop(
      id.toLong(),
      name,
      lat,
      lon,
      null
   )
}

fun DbStop.toStop(): Stop {
   return Stop(
      id.toInt(),
      name,
      lat,
      lon
   )
}
