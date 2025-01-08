package com.matejdro.mbus.lines

import com.matejdro.mbus.schedule.model.Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData

class FakeLinesRepository : LinesRepository {
   private val providedLines = MutableStateFlow<Outcome<List<Line>>?>(null)
   var numLoads = 0

   fun provideLines(stops: Outcome<List<Line>>) {
      providedLines.value = stops
   }

   override fun getAllLines(): Flow<Outcome<List<Line>>> {
      numLoads++
      return providedLines.map { it ?: error("fake lines not provided") }
   }

   override fun getSomeLines(ids: Collection<Int>): Flow<Outcome<List<Line>>> {
      return getAllLines().map { outcome ->
         outcome.mapData { list ->
            list.filter { ids.contains(it.id) }
         }
      }
   }
}
