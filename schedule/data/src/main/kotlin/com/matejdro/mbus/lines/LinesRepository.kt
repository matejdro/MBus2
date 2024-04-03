package com.matejdro.mbus.lines

import com.matejdro.mbus.schedule.model.Line
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface LinesRepository {
   fun getAllLines(): Flow<Outcome<List<Line>>>
}
