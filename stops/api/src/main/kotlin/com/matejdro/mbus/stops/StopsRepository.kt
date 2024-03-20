package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface StopsRepository {
   fun getAllStops(): Flow<Outcome<List<Stop>>>
}
