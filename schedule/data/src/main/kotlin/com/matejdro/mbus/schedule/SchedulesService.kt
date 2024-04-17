package com.matejdro.mbus.schedule

import com.matejdro.mbus.live.models.LiveArrivalsDto
import com.matejdro.mbus.schedule.models.LinesDto
import com.matejdro.mbus.schedule.models.StopScheduleDto
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate

interface SchedulesService {
   @GET("GetStopPointInfo")
   suspend fun getSchedule(
      @Query("StopPointId")
      stopId: Int,
      @Query("Date")
      date: LocalDate,
   ): StopScheduleDto

   @GET("GetLines")
   suspend fun getLines(): LinesDto

   @GET("GetArrivalsForStopPoint")
   suspend fun getLiveArrivalsForStopPoint(
      @Query("StopPointId")
      stopId: Int,
   ): LiveArrivalsDto
}
