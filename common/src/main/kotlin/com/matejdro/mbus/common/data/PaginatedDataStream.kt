package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData

interface PaginatedDataStream<T : PaginationResult> {
   val data: Flow<Outcome<T>>
   fun nextPage()
}

interface PaginationResult {
   val hasAnyDataLeft: Boolean
}

fun <F : PaginationResult, T : PaginationResult> PaginatedDataStream<F>.mapData(mapper: (F) -> T): PaginatedDataStream<T> {
   return object : PaginatedDataStream<T> {
      override val data: Flow<Outcome<T>>
         get() = this@mapData.data.map { it.mapData(mapper) }

      override fun nextPage() {
         this@mapData.nextPage()
      }
   }
}
