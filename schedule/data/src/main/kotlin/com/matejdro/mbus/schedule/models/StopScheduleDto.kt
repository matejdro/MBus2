package com.matejdro.mbus.schedule.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalTime

@JsonClass(generateAdapter = true)
data class StopScheduleDto(
   @Json(name = "Schedules")
   val schedules: List<Schedule>,
   @Json(name = "StaticData")
   val staticData: StaticData,
) {
   @JsonClass(generateAdapter = true)
   data class Schedule(
      @Json(name = "LineId")
      val lineId: Int,
      @Json(name = "LineShortName")
      val lineShortName: String,
      @Json(name = "RouteAndSchedules")
      val routeAndSchedules: List<RouteAndSchedule>,
   ) {
      @JsonClass(generateAdapter = true)
      data class RouteAndSchedule(
         @Json(name = "Departures")
         val departures: List<LocalTime>,
         @Json(name = "Direction")
         val direction: String,
      )
   }

   @JsonClass(generateAdapter = true)
   data class StaticData(
      @Json(name = "Description")
      val description: String,
      @Json(name = "StopPointImgPath")
      val image: String?,
      @Json(name = "StopPointName")
      val name: String,
   )
}
