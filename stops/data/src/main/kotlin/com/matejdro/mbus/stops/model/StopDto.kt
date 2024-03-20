package com.matejdro.mbus.stops.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StopDto(
   @Json(name = "StopId")
   val id: Int,
   @Json(name = "Name")
   val name: String,
   @Json(name = "Lat")
   val lat: Double?,
   @Json(name = "Lon")
   val lon: Double?,
)

fun StopDto.toStop(): Stop? {
   if (lat == null || lon == null) return null

   return Stop(
      id,
      name,
      lat,
      lon
   )
}
