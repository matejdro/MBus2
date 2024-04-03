package com.matejdro.mbus.schedule

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.schedule.models.toArrival
import com.matejdro.mbus.sqldelight.generated.DbArrival
import com.matejdro.mbus.sqldelight.generated.DbArrivalQueries
import com.matejdro.mbus.stops.StopsRepository
import com.matejdro.mbus.stops.model.Stop
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.flowOnDefault
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.core.time.TimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@ContributesBinding(ApplicationScope::class)
class ScheduleRepositoryImpl @Inject constructor(
   private val service: SchedulesService,
   private val timeProvider: TimeProvider,
   private val stopsRepository: StopsRepository,
   private val dbArrivalQueries: DbArrivalQueries,
) : ScheduleRepository {
   override fun getScheduleForStop(stopId: Int): PaginatedDataStream<StopSchedule> {
      return object : PaginatedDataStream<StopSchedule> {
         val nextPageChannel = Channel<Unit>(1)
         val maxTime = MutableStateFlow<LocalDateTime>(LocalDateTime.MIN)

         val dbFlow = maxTime.flatMapLatest { maxTime ->
            val nowLocal = timeProvider.currentLocalDateTime()
            val minTimeIsoString = nowLocal.minusMinutes(CUTOFF_POINT_MINUTES_BEFORE_NOW)
               .let { DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it) }
            val maxTimeString = maxTime.toIsoString()

            dbArrivalQueries.selectAllOnStop(
               stopId.toLong(),
               minTimeIsoString,
               maxTimeString
            ).asFlow()
         }.map { query ->
            query.awaitAsList().map { it.toArrival() }
         }

         val statusFlow: Flow<Outcome<ScheduleMetadata>> = flow {
            var currentDate = timeProvider.currentLocalDate()
            var loadingMore = false

            while (currentCoroutineContext().isActive) {
               val nextDay = currentDate.plusDays(1)
               maxTime.value = nextDay.atStartOfDay()

               loadDataForADay(stopId, currentDate, loadingMore)

               nextPageChannel.receive()
               currentDate = nextDay
               loadingMore = true
            }
         }

         override val data: Flow<Outcome<StopSchedule>>
            get() = combine(statusFlow, dbFlow) { status, data ->
               status.mapData {
                  StopSchedule(
                     data,
                     it.stopName,
                     it.stopImage,
                     it.stopDescription,
                     it.hasAnyDataLeft
                  )
               }
            }.flowOnDefault()

         override fun nextPage() {
            nextPageChannel.trySend(Unit)
         }
      }
   }

   private suspend fun FlowCollector<Outcome<ScheduleMetadata>>.loadDataForADay(
      stopId: Int,
      currentDate: LocalDate,
      loadingMore: Boolean,
   ) {
      val now = timeProvider.currentInstant()

      val existingStopMetadata =
         requireNotNull(stopsRepository.getStop(stopId)) { "Stop should not be null, how did user get here?" }

      val dayStart = currentDate.atStartOfDay().toIsoString()
      val dayEnd = currentDate.plusDays(1).atStartOfDay().toIsoString()

      val existingData = dbArrivalQueries.selectAllOnStop(
         stopId.toLong(),
         dayStart,
         dayEnd
      ).executeAsList()

      val description = existingStopMetadata.description
      val cacheExpirationDate = existingStopMetadata.lastScheduleUpdate?.plus(CACHE_DURATION)

      val existingMetadata = ScheduleMetadata(
         existingStopMetadata.name,
         existingStopMetadata.imageUrl,
         description.orEmpty(),
         true
      )

      if (existingData.isNotEmpty() &&
         cacheExpirationDate != null &&
         cacheExpirationDate > now
      ) {
         emit(Outcome.Success(existingMetadata))
      } else {
         val loadingStyle = if (loadingMore) {
            LoadingStyle.ADDITIONAL_DATA
         } else {
            LoadingStyle.NORMAL
         }

         emit(Outcome.Progress(existingMetadata, style = loadingStyle))

         try {
            loadScheduleFromNewtwork(existingStopMetadata, currentDate, now)
         } catch (e: CauseException) {
            emit(Outcome.Error(e, existingMetadata))
         }
      }
   }

   private suspend fun FlowCollector<Outcome<ScheduleMetadata>>.loadScheduleFromNewtwork(
      stop: Stop,
      today: LocalDate,
      now: Instant,
   ) {
      val onlineSchedule = service.getSchedule(stop.id, today)

      val dayStart = today.atStartOfDay().toIsoString()
      val dayEnd = today.plusDays(1).atStartOfDay().toIsoString()

      dbArrivalQueries.transaction {
         dbArrivalQueries.clearStop(
            stop.id.toLong(),
            dayStart,
            dayEnd
         )

         for (lineSchedule in onlineSchedule.schedules) {
            for (route in lineSchedule.routeAndSchedules) {
               for (departure in route.departures) {
                  dbArrivalQueries.insert(
                     DbArrival(
                        lineSchedule.lineId.toLong(),
                        stop.id.toLong(),
                        departure.atDate(today).toIsoString(),
                        route.direction
                     )
                  )
               }
            }
         }
      }

      stopsRepository.update(
         stop.copy(
            description = onlineSchedule.staticData.description,
            imageUrl = onlineSchedule.staticData.image,
            lastScheduleUpdate = now
         )
      )

      emit(
         Outcome.Success(
            ScheduleMetadata(
               stop.name,
               onlineSchedule.staticData.image,
               onlineSchedule.staticData.description,
               true
            )
         )
      )
   }
}

private fun LocalDateTime.toIsoString(): String {
   return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this)
}

private data class ScheduleMetadata(
   val stopName: String,
   val stopImage: String?,
   val stopDescription: String,
   val hasAnyDataLeft: Boolean,
)

private const val CUTOFF_POINT_MINUTES_BEFORE_NOW = 10L
private val CACHE_DURATION = 48.hours.toJavaDuration()
