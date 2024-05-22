package com.matejdro.mbus.favorites.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.logging.ActionLogger
import com.matejdro.mbus.favorites.FavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.navigation.keys.FavoriteScheduleScreenKey
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
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
class FavoriteScheduleViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val favoritesRepository: FavoritesRepository,
   private val timeProvider: TimeProvider,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<FavoriteScheduleScreenKey>(resources.scope) {
   private val _schedule = MutableStateFlow<Outcome<FavoriteScheduleUiState>>(Outcome.Progress())
   val schedule: StateFlow<Outcome<FavoriteScheduleUiState>> = _schedule

   private var lastPaginator: PaginatedDataStream<FavoriteSchedule>? = null

   override fun onServiceRegistered() {
      actionLogger.logAction { "FavoriteScheduleViewModel.onServiceRegistered()" }
      load(timeProvider.currentLocalDateTime(), false)
   }

   fun loadNextPage() {
      actionLogger.logAction { "FavoriteScheduleViewModel.loadNextPage()" }
      val existingData = _schedule.value.data
      if (!existingData?.arrivals.isNullOrEmpty() && existingData?.hasAnyDataLeft != false) {
         lastPaginator?.nextPage()
      }
   }

   fun setFilter(filter: Set<Int>) = coroutineScope.launch {
      actionLogger.logAction { "FavoriteScheduleViewModel.setFilter(filter = $filter)" }
      favoritesRepository.setWhitelistedLines(key.favoriteId, filter)
   }

   fun changeDate(newDateTime: LocalDateTime) {
      actionLogger.logAction { "FavoriteScheduleViewModel.changeDate(newDateTime = $newDateTime)" }
      load(newDateTime, true)
   }

   private fun load(date: LocalDateTime, customTimeSet: Boolean) = resources.launchResourceControlTask(_schedule) {
      val paginator = favoritesRepository.getScheduleForFavorite(key.favoriteId, date)
      lastPaginator = paginator

      emitAll(
         paginator.data.map { outcome ->
            outcome.mapData {
               with(it) {
                  FavoriteScheduleUiState(
                     favorite = favorite,
                     arrivals = arrivals,
                     hasAnyDataLeft = hasAnyDataLeft,
                     allLines = allLines,
                     whitelistedLines = whitelistedLines,
                     selectedTime = date.atZone(timeProvider.systemDefaultZoneId()),
                     customTimeSet = customTimeSet,
                  )
               }
            }
         }
      )
   }
}

data class FavoriteScheduleUiState(
   val favorite: Favorite,
   val arrivals: List<Arrival>,
   val hasAnyDataLeft: Boolean,
   val allLines: List<Line>,
   val whitelistedLines: Set<Int>,
   val selectedTime: ZonedDateTime,
   val customTimeSet: Boolean,
)
