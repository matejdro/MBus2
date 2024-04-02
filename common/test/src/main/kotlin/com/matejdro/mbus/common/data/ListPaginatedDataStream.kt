package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome

class ListPaginatedDataStream<P : PaginationResult, T>(
   private val pages: List<List<T>>,
   mapper: (List<T>, hasAnyDataLeft: Boolean) -> Outcome<P>,
) : PaginatedDataStream<P> {

   private val pagesToShow = MutableStateFlow<Int>(1)

   override val data: Flow<Outcome<P>> = pagesToShow.map { pagesToShow ->
      val allData = pages.take(pagesToShow).flatten()
      mapper(allData, pagesToShow > pages.size)
   }

   override fun nextPage() {
      pagesToShow.value++
   }
}
