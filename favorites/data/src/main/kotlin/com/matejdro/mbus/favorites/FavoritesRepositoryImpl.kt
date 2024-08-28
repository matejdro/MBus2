package com.matejdro.mbus.favorites

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.common.data.PaginationResult
import com.matejdro.mbus.common.data.flattenOutcomes
import com.matejdro.mbus.common.data.mapData
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.LineStop
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.favorites.model.toFavorite
import com.matejdro.mbus.schedule.ScheduleRepository
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.sqldelight.generated.DbFavorite
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.flowOnDefault
import dispatch.core.withDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import java.time.LocalDateTime
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class FavoritesRepositoryImpl @Inject constructor(
   private val db: DbFavoriteQueries,
   private val scheduleRepository: ScheduleRepository,
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
         db.insert(DbFavorite(0L, name, "", ""))
         db.lastInsertRowId().awaitAsOne()
      }
   }

   override suspend fun deleteFavourite(favouriteId: Long) {
      return withDefault {
         db.delete(favouriteId)
      }
   }

   override suspend fun updateFavoriteName(favoriteId: Long, newName: String) {
      withDefault {
         db.updateName(newName, favoriteId)
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

   override fun getScheduleForFavorite(favoriteId: Long, from: LocalDateTime): PaginatedDataStream<FavoriteSchedule> {
      var childStreams: List<PaginatedDataStream<StopScheduleWithId>> = emptyList()

      return object : PaginatedDataStream<FavoriteSchedule> {
         override val data: Flow<Outcome<FavoriteSchedule>> = flow<Outcome<FavoriteSchedule>> {
            val dbFavoriteFlow = db.selectSingle(favoriteId).asFlow()

            emitAll(
               dbFavoriteFlow.flatMapLatest { dbFavoriteQuery ->
                  val dbFavorite = dbFavoriteQuery.executeAsOne()
                  val favorite = dbFavorite.toFavorite()

                  val whitelistedLines = dbFavorite.getWhitelistedLines()

                  childStreams = favorite.stopsIds.map { id ->
                     scheduleRepository.getScheduleForStop(id, from, ignoreLineWhitelist = true).mapData {
                        StopScheduleWithId(id, it)
                     }
                  }

                  combine(childStreams.map { it.data }) { childOutcomes ->
                     childOutcomes.toList().flattenOutcomes().mapData { nullableStops ->
                        val stops = nullableStops.filterNotNull()

                        val allLines =
                           stops.flatMap { (stopId, stopSchedule) ->
                              val stopInfo = StopInfo(
                                 stopId,
                                 stopSchedule.stopName,
                                 stopSchedule.stopDescription,
                                 stopSchedule.stopImage
                              )

                              stopSchedule.allLines.map { LineStop(it, stopInfo) }
                           }

                        FavoriteSchedule(
                           favorite = favorite,
                           arrivals = stops.map { (stopId, stop) ->
                              stop.arrivals
                                 .filter {
                                    whitelistedLines.isEmpty() || whitelistedLines.contains(it.line.id to stopId)
                                 }.map {
                                    it.copy(
                                       direction = "${stop.stopName}\n${it.direction}"
                                    )
                                 }
                           }.flatten().sortedBy { it.arrival },
                           allLines = allLines,
                           hasAnyDataLeft = stops.any { it.hasAnyDataLeft },
                           whitelistedLines = allLines.filterTo(HashSet()) { lineStop ->
                              whitelistedLines.any {
                                 lineStop.line.id == it.first && lineStop.stop.id == it.second
                              }
                           }
                        )
                     }
                  }
               }
            )
         }.flowOnDefault()

         override fun nextPage() {
            childStreams.forEach { it.nextPage() }
         }
      }
   }

   override suspend fun setWhitelistedLines(favoriteId: Long, whitelistedLines: Set<LineStop>) {
      withDefault {
         db.updateWhitelist(whitelistedLines.joinToString(",") { "${it.line.id}-${it.stop.id}" }, favoriteId)
      }
   }
}

private fun DbFavorite.getWhitelistedLines() =
   lineWhitelist.split(",").mapNotNull { whitelistEntry ->
      if (whitelistEntry.isEmpty() || !whitelistEntry.contains("-")) {
         null
      } else {
         whitelistEntry.split("-")
            .let { it.elementAt(0).toInt() to it.elementAt(1).toInt() }
      }
   }.toSet()

private data class StopScheduleWithId(val stopId: Int, val stopSchedule: StopSchedule) : PaginationResult by stopSchedule
