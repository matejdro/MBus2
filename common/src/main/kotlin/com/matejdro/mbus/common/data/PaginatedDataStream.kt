package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface PaginatedDataStream<T> {
   val data: Flow<PaginationResult<T>>
   fun nextPage()

   data class PaginationResult<T>(
      val items: Outcome<T>,
      val hasAnyDataLeft: Boolean,
   )
}
