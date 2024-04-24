package com.matejdro.mbus.schedule.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.ScheduleRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.stops.StopsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.core.time.TimeProvider
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.inject.Inject

@Stable
class StopScheduleViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val scheduleRepository: ScheduleRepository,
   private val stopRepo: StopsRepository,
   private val timeProvider: TimeProvider,
) : SingleScreenViewModel<StopScheduleScreenKey>(resources.scope) {
   private val _schedule = MutableStateFlow<Outcome<ScheduleUiState>>(Outcome.Progress())
   val schedule: StateFlow<Outcome<ScheduleUiState>> = _schedule

   private var lastPaginator: PaginatedDataStream<StopSchedule>? = null

   override fun onServiceRegistered() {
      load(timeProvider.currentLocalDateTime(), false)
   }

   private fun load(date: LocalDateTime, customTimeSet: Boolean) = resources.launchResourceControlTask(_schedule) {
      val paginator = scheduleRepository.getScheduleForStop(key.stopId, date)
      lastPaginator = paginator

      emitAll(
         paginator.data.map { outcome ->
            outcome.mapData {
               with(it) {
                  ScheduleUiState(
                     arrivals = arrivals,
                     stopName = stopName,
                     stopImage = stopImage,
                     stopDescription = stopDescription,
                     hasAnyDataLeft = hasAnyDataLeft,
                     allLines = allLines,
                     whitelistedLines = whitelistedLines,
                     selectedTime = date.atZone(timeProvider.systemDefaultZoneId()),
                     customTimeSet = customTimeSet
                  )
               }
            }
         }
      )
   }

   fun loadNextPage() {
      val existingData = _schedule.value.data
      if (!existingData?.arrivals.isNullOrEmpty() && existingData?.hasAnyDataLeft != false) {
         lastPaginator?.nextPage()
      }
   }

   fun setFilter(filter: Set<Int>) = coroutineScope.launch {
      stopRepo.setWhitelistedLines(key.stopId, filter)
   }

   fun changeDate(newDateTime: LocalDateTime) {
      load(newDateTime, true)
   }
}

data class ScheduleUiState(
   val arrivals: List<Arrival>,
   val stopName: String,
   val stopImage: String?,
   val stopDescription: String,
   val hasAnyDataLeft: Boolean,
   val allLines: List<Line>,
   val whitelistedLines: Set<Int>,
   val selectedTime: ZonedDateTime,
   val customTimeSet: Boolean,
)
