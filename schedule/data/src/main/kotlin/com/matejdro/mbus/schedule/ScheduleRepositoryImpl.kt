package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.withDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.TimeProvider
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class ScheduleRepositoryImpl @Inject constructor(
   private val service: SchedulesService,
   private val timeProvider: TimeProvider,
) : ScheduleRepository {
   override fun getScheduleForStop(stopId: Int): PaginatedDataStream<StopSchedule> {
      return object : PaginatedDataStream<StopSchedule> {
         override val data: Flow<PaginatedDataStream.PaginationResult<StopSchedule>>
            get() = suspend {
               withDefault {
                  val lines = service.getLines().lines.associate {
                     it.lineId to Line(it.lineId, it.code, it.color)
                  }

                  val cutoffPoint = timeProvider.currentLocalTime().minusMinutes(CUTOFF_POINT_MINUTES_BEFORE_NOW)

                  val today = timeProvider.currentLocalDate()
                  val todaysSchedule = service.getSchedule(stopId, today)

                  val arrivals = todaysSchedule.schedules
                     .flatMap { schedule ->
                        val line = lines[schedule.lineId] ?: error("Unknown line ${schedule.lineId}")
                        schedule.routeAndSchedules.flatMap { routeList ->
                           routeList.departures
                              .filter { it >= cutoffPoint }
                              .map {
                                 Arrival(line, it.atDate(today), routeList.direction)
                              }
                        }
                     }
                     .sortedBy { it.arrival }

                  PaginatedDataStream.PaginationResult(
                     Outcome.Success(
                        StopSchedule(
                           arrivals,
                           todaysSchedule.staticData.name,
                           todaysSchedule.staticData.image,
                           todaysSchedule.staticData.description
                        )
                     ),
                     false
                  )
               }
            }.asFlow()

         override fun nextPage() {
            // No pagination support for now
         }
      }
   }
}

private const val CUTOFF_POINT_MINUTES_BEFORE_NOW = 10L
