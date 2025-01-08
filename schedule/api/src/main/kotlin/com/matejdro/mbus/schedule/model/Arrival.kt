package com.matejdro.mbus.schedule.model

import si.inova.kotlinova.core.data.Immutable
import java.time.LocalDateTime

@Immutable
data class Arrival(
   val line: Line,
   val arrival: LocalDateTime,
   val direction: String,
   val liveDelayMin: Int? = null,
)
