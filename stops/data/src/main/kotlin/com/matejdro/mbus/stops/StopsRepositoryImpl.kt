package com.matejdro.mbus.stops

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.stops.model.toDbStop
import com.matejdro.mbus.stops.model.toStop
import com.matejdro.mbus.stops.sqldelight.generated.DbStopQueries
import com.squareup.anvil.annotations.ContributesBinding
import dispatch.core.withDefault
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
class StopsRepositoryImpl @Inject constructor(
   private val stopsService: StopsService,
   private val dbStopQueries: DbStopQueries,
   private val dataStore: DataStore<Preferences>,
   private val timeProvider: TimeProvider,
) : StopsRepository {
   val loadMutext = Mutex()

   override fun getAllStops(): Flow<Outcome<List<Stop>>> {
      val dbFlow = dbStopQueries.selectAll().asFlow().map { query ->
         withDefault {
            query.awaitAsList().map { it.toStop() }
         }
      }

      val statusFlow = flow<Outcome<Unit>> {
         if (loadMutext.isLocked) {
            emit(Outcome.Progress())
         }

         loadMutext.withLock {
            val lastLoad = dataStore.data.first()[LAST_UPDATE_PREFERENCE]?.let { Instant.ofEpochMilli(it) } ?: Instant.MIN
            val now = timeProvider.currentInstant()

            val cacheExpireTime = lastLoad + CACHE_DURATION
            if (cacheExpireTime < now) {
               emit(Outcome.Progress())

               val loadingStatus = catchIntoOutcome {
                  withDefault {
                     val onlineStops = stopsService.getAllStops().stops.mapNotNull {
                        it.toDbStop()
                     }

                     dbStopQueries.transaction {
                        dbStopQueries.clear()
                        for (stop in onlineStops) {
                           dbStopQueries.insert(stop)
                        }
                     }

                     dataStore.edit {
                        it[LAST_UPDATE_PREFERENCE] = now.toEpochMilli()
                     }
                  }

                  Outcome.Success(Unit)
               }

               emit(loadingStatus)
            } else {
               emit(Outcome.Success(Unit))
            }
         }
      }

      return combine(dbFlow, statusFlow) { data, status ->
         Outcome.Success(data).downgradeTo(status)
      }
   }
}

private val LAST_UPDATE_PREFERENCE = longPreferencesKey("last_stops_update")
private val CACHE_DURATION = 48.hours.toJavaDuration()
