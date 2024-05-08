package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome

suspend fun <T> Flow<Outcome<T>>.awaitFirstSuccess(): T {
   return mapNotNull {
      when (it) {
         is Outcome.Error -> throw it.exception
         is Outcome.Progress -> null
         is Outcome.Success -> it
      }
   }.first().data
}

/**
 * Flatten a list of [Outcome] into Outcome of a list.
 *
 * * If any of the input Outcomes is Failure, returning outcome will be a Failure as well (with partial data).
 * * If any of the input Outcomes is Progress, returning outcome will be a progress as well (with partial data).
 * * Otherwise, returning outcome will be a Success with full data of all child outcomes.
 */
fun <T> Collection<Outcome<T>>.flattenOutcomes(): Outcome<List<T?>> {
   var exception: CauseException? = null
   var anyLoading = false

   val datas = map {
      when (it) {
         is Outcome.Error -> {
            exception = it.exception
         }

         is Outcome.Progress -> {
            anyLoading = true
         }

         is Outcome.Success -> {}
      }

      it.data
   }

   return if (exception != null) {
      Outcome.Error(exception!!, datas)
   } else if (anyLoading) {
      Outcome.Progress(datas)
   } else {
      Outcome.Success(datas)
   }
}
