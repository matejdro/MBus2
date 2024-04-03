package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.data.awaitFirstSuccess
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.lines.LinesRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.StopSchedule
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.flowOnDefault
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.TimeProvider
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class ScheduleRepositoryImpl @Inject constructor(
   private val service: SchedulesService,
   private val timeProvider: TimeProvider,
   private val linesRepository: LinesRepository,
) : ScheduleRepository {
   override fun getScheduleForStop(stopId: Int): PaginatedDataStream<StopSchedule> {
      return object : PaginatedDataStream<StopSchedule> {
         val nextPageChannel = Channel<Unit>(1)

         override val data: Flow<Outcome<StopSchedule>>
            get() = flow {
               var currentDate = timeProvider.currentLocalDate()
               val initialSchedule = loadSchedule(timeProvider.currentLocalDateTime(), stopId)
               emit(Outcome.Success(initialSchedule))

               var allArrivals = initialSchedule.arrivals
               while (currentCoroutineContext().isActive) {
                  nextPageChannel.receive()
                  currentDate = currentDate.plus(1, ChronoUnit.DAYS)

                  emit(Outcome.Progress(initialSchedule.copy(arrivals = allArrivals), style = LoadingStyle.ADDITIONAL_DATA))

                  allArrivals += loadSchedule(currentDate.atStartOfDay(), stopId, wholeDay = true).arrivals
                  emit(Outcome.Success(initialSchedule.copy(arrivals = allArrivals)))
               }
            }.flowOnDefault()

         override fun nextPage() {
            nextPageChannel.trySend(Unit)
         }
      }
   }

   private suspend fun loadSchedule(now: LocalDateTime, stopId: Int, wholeDay: Boolean = false): StopSchedule {
      val lines = linesRepository.getAllLines().awaitFirstSuccess().associateBy {
         it.id
      }

      val cutoffPoint = if (wholeDay) {
         LocalTime.MIN
      } else {
         now.toLocalTime().minusMinutes(CUTOFF_POINT_MINUTES_BEFORE_NOW)
      }

      val today = now.toLocalDate()
      val todaysSchedule = service.getSchedule(stopId, today)

      val arrivals = todaysSchedule.schedules
         .flatMap { schedule ->
            val line = lines[schedule.lineId] ?: return@flatMap emptyList()
            schedule.routeAndSchedules.flatMap { routeList ->
               routeList.departures
                  .filter { it >= cutoffPoint }
                  .map {
                     Arrival(line, it.atDate(today), routeList.direction)
                  }
            }
         }
         .sortedBy { it.arrival }

      return StopSchedule(
         arrivals,
         todaysSchedule.staticData.name,
         todaysSchedule.staticData.image,
         todaysSchedule.staticData.description,
         true
      )
   }
}

private const val CUTOFF_POINT_MINUTES_BEFORE_NOW = 10L
