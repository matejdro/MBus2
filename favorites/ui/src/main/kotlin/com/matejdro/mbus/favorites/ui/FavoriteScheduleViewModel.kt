package com.matejdro.mbus.favorites.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.logging.ActionLogger
import com.matejdro.mbus.favorites.FavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.LineStop
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.navigation.keys.FavoriteScheduleScreenKey
import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
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

   fun setFilter(filter: Set<LineStop>) = coroutineScope.launch {
      actionLogger.logAction { "FavoriteScheduleViewModel.setFilter(filter = $filter)" }
      favoritesRepository.setWhitelistedLines(key.favoriteId, filter)
   }

   fun changeDate(newDateTime: LocalDateTime) {
      actionLogger.logAction { "FavoriteScheduleViewModel.changeDate(newDateTime = $newDateTime)" }
      load(newDateTime, true)
   }

   fun updateFavorite(newName: String, stopsToRemove: List<StopInfo>) = coroutineScope.launch {
      actionLogger.logAction { "FavoriteScheduleViewModel.updateFavorite(newName = $newName, stopsToRemove = $stopsToRemove)" }

      try {
         for (stop in stopsToRemove) {
            favoritesRepository.removeStopToFavourite(key.favoriteId, stop.id)
         }
         favoritesRepository.updateFavoriteName(key.favoriteId, newName)
      } catch (e: Exception) {
         _schedule.update { Outcome.Error(if (e is CauseException) e else UnknownCauseException(cause = e), it.data) }
      }
   }

   fun deleteFavorite() = resources.launchResourceControlTask(_schedule) {
      actionLogger.logAction { "FavoriteScheduleViewModel.deleteFavorite()" }

      favoritesRepository.deleteFavourite(key.favoriteId)

      update {
         Outcome.Success(requireNotNull(it.data).copy(closeScreenAfterDeletion = true))
      }
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
                     allStops = allLines.map { it.stop }.distinct()
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
   val allLines: List<LineStop>,
   val allStops: List<StopInfo>,
   val whitelistedLines: Set<LineStop>,
   val selectedTime: ZonedDateTime,
   val customTimeSet: Boolean,
   val closeScreenAfterDeletion: Boolean = false,
)
