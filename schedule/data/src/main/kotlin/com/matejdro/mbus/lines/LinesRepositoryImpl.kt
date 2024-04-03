package com.matejdro.mbus.lines

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import app.cash.sqldelight.Query
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.schedule.SchedulesService
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.models.toDbLine
import com.matejdro.mbus.schedule.models.toLine
import com.matejdro.mbus.sqldelight.generated.DbLine
import com.matejdro.mbus.sqldelight.generated.DbLineQueries
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.catchIntoOutcome
import si.inova.kotlinova.core.outcome.downgradeTo
import si.inova.kotlinova.core.time.TimeProvider
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Singleton
@ContributesBinding(ApplicationScope::class)
class LinesRepositoryImpl @Inject constructor(
   private val schedulesService: SchedulesService,
   private val dbLineQueries: DbLineQueries,
   private val dataStore: DataStore<Preferences>,
   private val timeProvider: TimeProvider,
) : LinesRepository {
   private val loadMutext = Mutex()

   override fun getAllLines(): Flow<Outcome<List<Line>>> {
      return queryLines(dbLineQueries.selectAll())
   }

   override fun getSomeLines(ids: Collection<Int>): Flow<Outcome<List<Line>>> {
      return queryLines(dbLineQueries.selectSpecific(ids.map { it.toLong() }))
   }

   private fun queryLines(query: Query<DbLine>): Flow<Outcome<List<Line>>> {
      val dbFlow = query.asFlow().map { updatedQuery ->
         dispatch.core.withDefault {
            updatedQuery.awaitAsList().map { it.toLine() }
         }
      }

      val statusFlow = flow {
         if (loadMutext.isLocked) {
            this.emit(Outcome.Progress())
         }

         loadMutext.withLock {
            val lastLoad = dataStore.data.first()[LAST_UPDATE_PREFERENCE]?.let { Instant.ofEpochMilli(it) } ?: Instant.MIN
            val now = timeProvider.currentInstant()

            val cacheExpireTime = lastLoad + CACHE_DURATION
            if (cacheExpireTime < now) {
               this.emit(Outcome.Progress())

               val loadingStatus = catchIntoOutcome {
                  dispatch.core.withDefault {
                     val onlineLines = schedulesService.getLines().lines.mapNotNull {
                        it.toDbLine()
                     }

                     dbLineQueries.transaction {
                        dbLineQueries.clear()
                        for (line in onlineLines) {
                           dbLineQueries.insert(line)
                        }
                     }

                     dataStore.edit {
                        it[LAST_UPDATE_PREFERENCE] = now.toEpochMilli()
                     }
                  }

                  Outcome.Success(Unit)
               }

               this.emit(loadingStatus)
            } else {
               this.emit(Outcome.Success(Unit))
            }
         }
      }
      return combine(dbFlow, statusFlow) { data, status ->
         Outcome.Success(data).downgradeTo(status)
      }
   }
}

private val LAST_UPDATE_PREFERENCE = longPreferencesKey("last_lines_update")
private val CACHE_DURATION = 48.hours.toJavaDuration()
