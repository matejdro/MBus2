package com.matejdro.mbus.live.models

import com.matejdro.mbus.schedule.model.Arrival
import kotlinx.coroutines.flow.Flow

interface LiveArrivalRepository {
   fun addLiveArrivals(
      stopId: Int,
      originalArrivals: List<Arrival>,
   ): Flow<List<Arrival>>
}
