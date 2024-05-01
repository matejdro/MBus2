package com.matejdro.mbus.favorites

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.toFavorite
import com.matejdro.mbus.sqldelight.generated.DbFavorite
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.flowOnDefault
import dispatch.core.withDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class FavoritesRepositoryImpl @Inject constructor(
   private val db: DbFavoriteQueries,
) : FavoritesRepository {
   override fun getListOfFavorites(): Flow<List<Favorite>> {
      return db.selectAll().asFlow().map { query ->
         query.awaitAsList().map {
            it.toFavorite()
         }
      }.flowOnDefault()
   }

   override suspend fun createFavourite(name: String): Long {
      return withDefault {
         db.insert(DbFavorite(0L, name, ""))
         db.lastInsertRowId().awaitAsOne()
      }
   }

   override suspend fun addStopToFavourite(favouriteId: Long, stopId: Int) {
      withDefault {
         db.transaction {
            val favourite = db.selectSingle(favouriteId).executeAsOne()
            val newStops = if (favourite.stops.isEmpty()) {
               stopId.toString()
            } else {
               "${favourite.stops},$stopId"
            }

            db.updateStops(newStops, favouriteId.toLong())
         }
      }
   }

   override suspend fun removeStopToFavourite(favouriteId: Long, stopId: Int) {
      withDefault {
         db.transaction {
            val favourite = db.selectSingle(favouriteId).executeAsOne()
            val newStops = favourite.stops
               .replace("$stopId,", "")
               .replace(stopId.toString(), "")

            db.updateStops(newStops, favouriteId.toLong())
         }
      }
   }
}
