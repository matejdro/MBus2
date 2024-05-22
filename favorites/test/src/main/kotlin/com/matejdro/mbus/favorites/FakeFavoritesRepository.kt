package com.matejdro.mbus.favorites

import com.matejdro.mbus.common.data.ListPaginatedDataStream
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.Outcome
import java.time.LocalDateTime

class FakeFavoritesRepository : FavoritesRepository {
   private val favorites = MutableStateFlow<List<Favorite>>(emptyList())
   var whitelistedLines = emptySet<Int>()
      private set
   private val providedSchedules = HashMap<Long, FakeSchedules>()

   var lastRequestedDate: LocalDateTime? = null

   override fun getListOfFavorites(): Flow<List<Favorite>> {
      return favorites
   }

   override suspend fun createFavourite(name: String): Long {
      val newId = favorites.value.size.toLong()

      favorites.update {
         it + Favorite(newId, name, emptyList())
      }

      return newId
   }

   override suspend fun deleteFavourite(favouriteId: Long) {
      favorites.update { list ->
         list.filter { it.id != favouriteId }
      }
   }

   override suspend fun addStopToFavourite(favouriteId: Long, stopId: Int) {
      favorites.update { list ->
         list.map {
            if (it.id == favouriteId) {
               it.copy(stopsIds = it.stopsIds + stopId)
            } else {
               it
            }
         }
      }
   }

   override suspend fun removeStopToFavourite(favouriteId: Long, stopId: Int) {
      favorites.update { list ->
         list.map {
            if (it.id == favouriteId) {
               it.copy(stopsIds = it.stopsIds - stopId)
            } else {
               it
            }
         }
      }
   }

   fun provideSchedule(
      favorite: Favorite,
      includedStops: List<StopInfo>,
      vararg arrivals: List<Arrival>,
   ) {
      val allLines = arrivals.toList().flatten().map { it.line }.distinct()

      providedSchedules[favorite.id] = FakeSchedules(favorite, includedStops, arrivals.toList(), allLines)
   }

   override fun getScheduleForFavorite(favoriteId: Long, from: LocalDateTime): PaginatedDataStream<FavoriteSchedule> {
      lastRequestedDate = from
      val favorite = favorites.value.first { it.id == favoriteId }

      val schedulePages = providedSchedules.get(favoriteId) ?: error("Schedule for favorite $favoriteId not provided")
      return ListPaginatedDataStream(schedulePages.arrivals) { list, hasAnyDataLeft ->
         Outcome.Success(
            FavoriteSchedule(
               schedulePages.favorite,
               schedulePages.includedStops.filter { favorite.stopsIds.contains(it.id) },
               list.filter { whitelistedLines.isEmpty() || whitelistedLines.contains(it.line.id) },
               schedulePages.allLines,
               hasAnyDataLeft,
               whitelistedLines
            )
         )
      }
   }

   override suspend fun setWhitelistedLines(favoriteId: Long, whitelistedLines: Set<Int>) {
      this.whitelistedLines = whitelistedLines
   }

   fun setFavorites(favorites: List<Favorite>) {
      this.favorites.value = favorites
   }
}

private data class FakeSchedules(
   val favorite: Favorite,
   val includedStops: List<StopInfo>,
   val arrivals: List<List<Arrival>>,
   val allLines: List<Line>,
)
