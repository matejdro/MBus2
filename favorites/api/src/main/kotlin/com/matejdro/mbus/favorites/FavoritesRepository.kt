package com.matejdro.mbus.favorites

import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.LineStop
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface FavoritesRepository {
   fun getListOfFavorites(): Flow<List<Favorite>>

   suspend fun createFavourite(name: String): Long
   suspend fun deleteFavourite(favouriteId: Long)
   suspend fun addStopToFavourite(favouriteId: Long, stopId: Int)
   suspend fun removeStopToFavourite(favouriteId: Long, stopId: Int)

   fun getScheduleForFavorite(favoriteId: Long, from: LocalDateTime): PaginatedDataStream<FavoriteSchedule>
   suspend fun setWhitelistedLines(favoriteId: Long, whitelistedLines: Set<LineStop>)
}
