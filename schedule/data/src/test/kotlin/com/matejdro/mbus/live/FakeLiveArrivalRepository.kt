package com.matejdro.mbus.live

import com.matejdro.mbus.live.models.LiveArrivalRepository
import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLiveArrivalRepository : LiveArrivalRepository {
   var swapMap = emptyMap<Arrival, Arrival>()
   override fun addLiveArrivals(stopId: Int, originalArrivals: List<Arrival>): Flow<List<Arrival>> {
      return flowOf(
         originalArrivals.map {
            swapMap[it] ?: it
         }
      )
   }
}
