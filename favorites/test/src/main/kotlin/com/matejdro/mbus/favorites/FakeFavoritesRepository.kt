package com.matejdro.mbus.favorites

import com.matejdro.mbus.favorites.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeFavoritesRepository : FavoritesRepository {
   private val favorites = MutableStateFlow<List<Favorite>>(emptyList())

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

   fun setData(favorites: List<Favorite>) {
      this.favorites.value = favorites
   }
}
