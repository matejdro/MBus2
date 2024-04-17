package com.matejdro.mbus.schedule.model

import java.time.LocalDateTime

data class Arrival(
   val line: Line,
   val arrival: LocalDateTime,
   val direction: String,
   val liveDelayMin: Int? = null,
)
