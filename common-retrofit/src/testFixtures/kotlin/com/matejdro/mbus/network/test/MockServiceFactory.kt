package com.matejdro.mbus.network.test

import com.matejdro.mbus.network.di.NetworkProviders
import com.matejdro.mbus.network.exceptions.DefaultErrorHandler
import com.matejdro.mbus.network.services.BaseServiceFactory
import kotlinx.coroutines.test.TestScope
import si.inova.kotlinova.core.test.outcomes.ThrowingErrorReporter
import si.inova.kotlinova.retrofit.MockWebServerScope

fun MockWebServerScope.serviceFactory(testScope: TestScope): BaseServiceFactory {
   val moshi = NetworkProviders.provideMoshi(emptySet())

   return BaseServiceFactory(
      testScope,
      { moshi },
      { NetworkProviders.prepareDefaultOkHttpClient().build() },
      ThrowingErrorReporter(testScope),
      DefaultErrorHandler({ moshi }),
      baseUrl
   )
}
