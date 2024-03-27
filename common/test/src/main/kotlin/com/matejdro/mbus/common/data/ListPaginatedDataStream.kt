package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.downgradeTo

class ListPaginatedDataStream<P, T>(private val pages: List<List<T>>, mapper: (List<T>) -> P) : PaginatedDataStream<P> {

   private val pagesToShow = MutableStateFlow<Int>(1)
   private val currentStatus = MutableStateFlow<Outcome<Unit>>(Outcome.Success<Unit>(Unit))

   override val data: Flow<PaginatedDataStream.PaginationResult<P>> = combine(pagesToShow, currentStatus) { pagesToShow, status ->
      val allData = pages.take(pagesToShow).flatten()
      PaginatedDataStream.PaginationResult(Outcome.Success(mapper(allData)).downgradeTo(status), pagesToShow >= pages.size)
   }

   override fun nextPage() {
      pagesToShow.value++
   }

   fun setStatus(status: Outcome<Unit>) {
      currentStatus.value = status
   }
}
