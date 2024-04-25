package com.matejdro.mbus.live.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalTime

@JsonClass(generateAdapter = true)
data class LiveArrivalsDto(
   @Json(name = "ArrivalsForStopPoints")
   val arrivalsForStopPoints: List<LiveArrivalDto>,
) {
   @JsonClass(generateAdapter = true)
   data class LiveArrivalDto(
      @Json(name = "ArrivalTime")
      val arrivalTime: LocalTime,
      @Json(name = "DelayMin")
      val delayMin: Int?,
      @Json(name = "LineId")
      val lineId: Int,
   )
}
