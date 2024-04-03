package com.matejdro.mbus.stops.model

import com.matejdro.mbus.sqldelight.generated.DbStop
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

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
      id = id.toLong(),
      name = name,
      lat = lat,
      lon = lon,
      lastScheduleUpdate = null,
      imageUrl = null,
      description = null
   )
}

fun DbStop.toStop(): Stop {
   return Stop(
      id = id.toInt(),
      name = name,
      lat = lat,
      lon = lon,
      description = description,
      imageUrl = imageUrl,
      lastScheduleUpdate = lastScheduleUpdate?.let { Instant.ofEpochMilli(it) }
   )
}

fun Stop.toDbStop(): DbStop {
   return DbStop(
      id = id.toLong(),
      name = name,
      lat = lat,
      lon = lon,
      lastScheduleUpdate = lastScheduleUpdate?.toEpochMilli(),
      imageUrl = imageUrl,
      description = description,
   )
}
