package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.ListPaginatedDataStream
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.StopSchedule
import si.inova.kotlinova.core.outcome.Outcome

class FakeScheduleRepository : ScheduleRepository {
   private val providedSchedules = HashMap<Int, FakeSchedules>()
   override fun getScheduleForStop(stopId: Int): PaginatedDataStream<StopSchedule> {
      val schedulePages = providedSchedules.get(stopId) ?: error("Schedule for stop $stopId not provided")
      return ListPaginatedDataStream(schedulePages.arrivals) { list, hasAnyDataLeft ->
         Outcome.Success(
            StopSchedule(
               list,
               schedulePages.stopName,
               schedulePages.stopImage,
               schedulePages.stopDescription,
               hasAnyDataLeft
            )
         )
      }
   }

   fun provideSchedule(
      stopId: Int,
      stopName: String,
      stopImage: String?,
      stopDescription: String,
      vararg arrivals: List<Arrival>,
   ) {
      providedSchedules[stopId] = FakeSchedules(arrivals.toList(), stopName, stopImage, stopDescription)
   }

   private data class FakeSchedules(
      val arrivals: List<List<Arrival>>,
      val stopName: String,
      val stopImage: String?,
      val stopDescription: String,
   )
}
