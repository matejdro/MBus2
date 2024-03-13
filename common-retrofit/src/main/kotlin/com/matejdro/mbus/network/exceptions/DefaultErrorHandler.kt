package com.matejdro.mbus.network.exceptions

import retrofit2.Response
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.retrofit.callfactory.ErrorHandler
import javax.inject.Inject

class DefaultErrorHandler @Inject constructor() : ErrorHandler {
   override fun generateExceptionFromErrorBody(response: Response<*>, parentException: Exception): CauseException? {
      // TODO
      error("Parse errors from your backend here")
   }
}
