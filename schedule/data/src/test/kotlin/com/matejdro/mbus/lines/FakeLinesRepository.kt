package com.matejdro.mbus.lines

import com.matejdro.mbus.schedule.model.Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome

class FakeLinesRepository : LinesRepository {
   private var providedLines = MutableStateFlow<Outcome<List<Line>>?>(null)
   var numLoads = 0

   fun provideLines(stops: Outcome<List<Line>>) {
      providedLines.value = stops
   }

   override fun getAllLines(): Flow<Outcome<List<Line>>> {
      numLoads++
      return providedLines.map { it ?: error("fake lines not provided") }
   }
}
