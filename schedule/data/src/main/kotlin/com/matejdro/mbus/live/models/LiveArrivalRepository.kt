package com.matejdro.mbus.live.models

import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface LiveArrivalRepository {
   fun addLiveArrivals(
      stopId: Int,
      originalArrivals: List<Arrival>,
      deleteNonLiveArrivalsBefore: LocalDateTime,
   ): Flow<List<Arrival>>
}
