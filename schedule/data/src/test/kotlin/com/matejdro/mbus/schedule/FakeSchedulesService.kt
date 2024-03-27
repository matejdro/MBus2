package com.matejdro.mbus.schedule

import com.matejdro.mbus.schedule.models.LinesDto
import com.matejdro.mbus.schedule.models.StopScheduleDto
import si.inova.kotlinova.retrofit.FakeService
import si.inova.kotlinova.retrofit.ServiceTestingHelper
import java.time.LocalDate

class FakeSchedulesService(
   private val helper: ServiceTestingHelper = ServiceTestingHelper(),
) : SchedulesService, FakeService by helper {
   private val providedSchedules = HashMap<Pair<Int, LocalDate>, StopScheduleDto>()
   var providedLines: LinesDto? = null

   override suspend fun getSchedule(stopId: Int, date: LocalDate): StopScheduleDto {
      helper.intercept()

      return providedSchedules.get(stopId to date) ?: error("Schedule for stop $stopId on $date not provided")
   }

   override suspend fun getLines(): LinesDto {
      return providedLines ?: error("Fake lines not provided")
   }

   fun provideSchedule(stopId: Int, date: LocalDate, schedule: StopScheduleDto) {
      providedSchedules[stopId to date] = schedule
   }
}
