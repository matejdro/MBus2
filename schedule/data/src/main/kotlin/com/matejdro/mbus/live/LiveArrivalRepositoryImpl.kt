package com.matejdro.mbus.live

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.live.models.LiveArrivalRepository
import com.matejdro.mbus.schedule.SchedulesService
import com.matejdro.mbus.schedule.model.Arrival
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.flow.onlyFlowWhenUserPresent
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(ApplicationScope::class)
class LiveArrivalRepositoryImpl @Inject constructor(private val schedulesService: SchedulesService) : LiveArrivalRepository {
   override fun addLiveArrivals(stopId: Int, originalArrivals: List<Arrival>): Flow<List<Arrival>> {
      return tickerFlow().map { _ ->
         val liveArrivalForThisStop = withTimeoutOrNull(LOAD_TIMEOUT) {
            try {
               schedulesService.getLiveArrivalsForStopPoint(stopId).arrivalsForStopPoints
            } catch (ignored: NoNetworkException) {
               null
            }
         } ?: return@map originalArrivals

         originalArrivals.map { arrival ->
            val arrivalTime = arrival.arrival.toLocalTime()

            val matchingLiveArrival =
               liveArrivalForThisStop.firstOrNull { it.lineId == arrival.line.id && it.arrivalTime == arrivalTime }

            if (matchingLiveArrival != null && matchingLiveArrival.delayMin != null) {
               arrival.copy(
                  arrival = arrival.arrival.plusMinutes(matchingLiveArrival.delayMin.toLong()),
                  liveDelayMin = matchingLiveArrival.delayMin
               )
            } else {
               arrival
            }
         }
      }.onStart { emit(originalArrivals) }
   }

   private fun tickerFlow(): Flow<Unit> {
      return flow {
         while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(UPDATE_INTERVAL)
         }
      }.onlyFlowWhenUserPresent { emit(Unit) }
   }
}

private val UPDATE_INTERVAL = 1.minutes
private val LOAD_TIMEOUT = 5.seconds
