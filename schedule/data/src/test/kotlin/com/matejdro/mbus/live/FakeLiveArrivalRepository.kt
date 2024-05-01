package com.matejdro.mbus.live

import com.matejdro.mbus.live.models.LiveArrivalRepository
import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

class FakeLiveArrivalRepository : LiveArrivalRepository {
   var swapMap = emptyMap<Arrival, Arrival>()
   var lastDeleteNonLiveArrivalsBefore: LocalDateTime? = null

   override fun addLiveArrivals(
      stopId: Int,
      originalArrivals: List<Arrival>,
      deleteNonLiveArrivalsBefore: LocalDateTime,
   ): Flow<List<Arrival>> {
      lastDeleteNonLiveArrivalsBefore = deleteNonLiveArrivalsBefore

      return flowOf(
         originalArrivals.map {
            swapMap[it] ?: it
         }
      )
   }
}
