package com.matejdro.mbus.schedule.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class Arrival(
   val line: Line,
   val arrival: LocalDateTime,
   val direction: String,
   val liveDelayMin: Int? = null,
)
