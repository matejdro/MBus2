package com.matejdro.mbus.schedule.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.ScheduleRepository
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.stops.StopsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import javax.inject.Inject

@Stable
class StopScheduleViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val scheduleRepository: ScheduleRepository,
   private val stopRepo: StopsRepository,
) : SingleScreenViewModel<StopScheduleScreenKey>(resources.scope) {
   private val _schedule = MutableStateFlow<Outcome<StopSchedule>>(Outcome.Progress())
   val schedule: StateFlow<Outcome<StopSchedule>> = _schedule

   private var lastPaginator: PaginatedDataStream<StopSchedule>? = null

   override fun onServiceRegistered() {
      load()
   }

   private fun load() = resources.launchResourceControlTask(_schedule) {
      val paginator = scheduleRepository.getScheduleForStop(key.stopId)
      lastPaginator = paginator

      emitAll(paginator.data)
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
}

data class ScheduleState(
   val items: Int,
)
