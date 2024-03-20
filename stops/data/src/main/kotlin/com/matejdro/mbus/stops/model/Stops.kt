package com.matejdro.mbus.stops.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stops(
   @Json(name = "StopPoints")
   val stops: List<StopDto>,
)
