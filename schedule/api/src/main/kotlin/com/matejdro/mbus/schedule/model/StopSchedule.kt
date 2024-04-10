package com.matejdro.mbus.schedule.model

import com.matejdro.mbus.common.data.PaginationResult

data class StopSchedule(
   val arrivals: List<Arrival>,
   val stopName: String,
   val stopImage: String?,
   val stopDescription: String,
   override val hasAnyDataLeft: Boolean,
   val allLines: List<Line>,
   val whitelistedLines: Set<Int> = emptySet(),
) : PaginationResult
