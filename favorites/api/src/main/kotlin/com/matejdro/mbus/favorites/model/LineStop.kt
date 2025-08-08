package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.schedule.model.Line
import androidx.compose.runtime.Immutable

@Immutable
data class LineStop(
   val line: Line,
   val stop: StopInfo,
)
