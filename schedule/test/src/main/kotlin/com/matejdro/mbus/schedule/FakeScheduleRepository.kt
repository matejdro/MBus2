package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.ListPaginatedDataStream
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.StopSchedule
import si.inova.kotlinova.core.outcome.Outcome
import java.time.LocalDateTime

class FakeScheduleRepository : ScheduleRepository {
   private val providedSchedules = HashMap<Int, FakeSchedules>()

   var lastRequestedDate: LocalDateTime? = null

   override fun getScheduleForStop(
      stopId: Int,
      from: LocalDateTime,
      ignoreLineWhitelist: Boolean,
   ): PaginatedDataStream<StopSchedule> {
      lastRequestedDate = from

      val schedulePages = providedSchedules.get(stopId) ?: error("Schedule for stop $stopId not provided")
      return ListPaginatedDataStream(schedulePages.arrivals) { list, hasAnyDataLeft ->
         Outcome.Success(
            StopSchedule(
               list,
               schedulePages.stopName,
               schedulePages.stopImage,
               schedulePages.stopDescription,
               hasAnyDataLeft,
               list.map { it.line }.distinctBy { it.id },
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
