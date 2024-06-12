package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.common.data.PaginationResult
import com.matejdro.mbus.schedule.model.Arrival

data class FavoriteSchedule(
   val favorite: Favorite,
   val arrivals: List<Arrival>,
   val allLines: List<LineStop>,
   override val hasAnyDataLeft: Boolean,
   val whitelistedLines: Set<LineStop> = emptySet(),
) : PaginationResult
