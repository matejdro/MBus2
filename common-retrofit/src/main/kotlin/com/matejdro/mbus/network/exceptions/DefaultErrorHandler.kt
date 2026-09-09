package com.matejdro.mbus.network.exceptions

import com.matejdro.mbus.common.exceptions.BackendErrorException
import com.matejdro.mbus.network.models.BackendError
import com.squareup.moshi.Moshi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import retrofit2.Response
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.retrofit.callfactory.ErrorHandler
import si.inova.kotlinova.retrofit.moshi.fromJson

class DefaultErrorHandler @Inject constructor(
   private val moshi: Provider<Moshi>,
) : ErrorHandler {
   override fun generateExceptionFromErrorBody(response: Response<*>, parentException: Exception): CauseException? {
      val errorResponse = requireNotNull(response.errorBody()).source().use { moshi().fromJson<BackendError>(it) }

      return BackendErrorException(errorResponse.toString(), parentException)
   }
}
