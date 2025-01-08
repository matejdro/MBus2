package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.schedule.model.Line
import si.inova.kotlinova.core.data.Immutable

@Immutable
data class LineStop(
   val line: Line,
   val stop: StopInfo,
)
