package com.matejdro.mbus.common.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
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
