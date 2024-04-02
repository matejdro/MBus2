package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface PaginatedDataStream<T : PaginationResult> {
   val data: Flow<Outcome<T>>
   fun nextPage()
}

interface PaginationResult {
   val hasAnyDataLeft: Boolean
}
