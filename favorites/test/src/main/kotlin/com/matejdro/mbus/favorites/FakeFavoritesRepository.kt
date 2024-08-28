package com.matejdro.mbus.favorites

import com.matejdro.mbus.common.data.ListPaginatedDataStream
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.LineStop
import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.Outcome
import java.time.LocalDateTime

class FakeFavoritesRepository : FavoritesRepository {
   private val favorites = MutableStateFlow<List<Favorite>>(emptyList())
   var whitelistedLines = emptySet<LineStop>()
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

   override suspend fun updateFavoriteName(favoriteId: Long, newName: String) {
      favorites.update { list ->
         list.map {
            if (it.id == favoriteId) {
               it.copy(name = newName)
            } else {
               it
            }
         }
      }
      providedSchedules[favoriteId]?.let {
         providedSchedules[favoriteId] = it.copy(favorite = it.favorite.copy(name = newName))
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
      includedLines: List<LineStop>,
      vararg arrivals: List<Arrival>,
   ) {
      providedSchedules[favorite.id] = FakeSchedules(favorite, arrivals.toList(), includedLines)
   }

   override fun getScheduleForFavorite(favoriteId: Long, from: LocalDateTime): PaginatedDataStream<FavoriteSchedule> {
      lastRequestedDate = from
      val favorite = favorites.value.first { it.id == favoriteId }

      val schedulePages = providedSchedules.get(favoriteId) ?: error("Schedule for favorite $favoriteId not provided")
      return ListPaginatedDataStream(schedulePages.arrivals) { list, hasAnyDataLeft ->
         Outcome.Success(
            FavoriteSchedule(
               schedulePages.favorite,
               list.filter { whitelistedLines.isEmpty() || whitelistedLines.any { wl -> wl.line.id == it.line.id } },
               schedulePages.allLines.filter { favorite.stopsIds.contains(it.stop.id) },
               hasAnyDataLeft,
               whitelistedLines
            )
         )
      }
   }

   override suspend fun setWhitelistedLines(favoriteId: Long, whitelistedLines: Set<LineStop>) {
      this.whitelistedLines = whitelistedLines
   }

   fun setFavorites(favorites: List<Favorite>) {
      this.favorites.value = favorites
   }
}

private data class FakeSchedules(
   val favorite: Favorite,
   val arrivals: List<List<Arrival>>,
   val allLines: List<LineStop>,
)
