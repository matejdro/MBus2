package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.common.data.PaginationResult
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line

data class FavoriteSchedule(
   val favorite: Favorite,
   val includedStops: List<StopInfo>,
   val arrivals: List<Arrival>,
   val allLines: List<Line>,
   override val hasAnyDataLeft: Boolean,
   val whitelistedLines: Set<Int> = emptySet(),
) : PaginationResult
