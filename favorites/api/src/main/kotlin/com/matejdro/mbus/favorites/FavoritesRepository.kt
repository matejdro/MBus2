package com.matejdro.mbus.favorites

import com.matejdro.mbus.favorites.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
   fun getListOfFavorites(): Flow<List<Favorite>>

   suspend fun createFavourite(name: String): Long
   suspend fun addStopToFavourite(favouriteId: Long, stopId: Int)
   suspend fun removeStopToFavourite(favouriteId: Long, stopId: Int)
}
