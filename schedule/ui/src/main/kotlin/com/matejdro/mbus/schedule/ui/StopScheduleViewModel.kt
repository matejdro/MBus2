package com.matejdro.mbus.schedule.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.ScheduleRepository
import com.matejdro.mbus.schedule.model.StopSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import javax.inject.Inject

@Stable
class StopScheduleViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val scheduleRepository: ScheduleRepository,
) : SingleScreenViewModel<StopScheduleScreenKey>(resources.scope) {
   private val _schedule = MutableStateFlow<Outcome<StopSchedule>>(Outcome.Progress())
   val schedule: StateFlow<Outcome<StopSchedule>> = _schedule

   override fun onServiceRegistered() {
      load()
   }

   private fun load() = resources.launchResourceControlTask(_schedule) {
      emitAll(
         scheduleRepository.getScheduleForStop(key.stopId).data.map { it.items }
      )
   }
}
