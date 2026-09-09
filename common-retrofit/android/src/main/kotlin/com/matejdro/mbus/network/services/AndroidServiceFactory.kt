package com.matejdro.mbus.network.services

import com.matejdro.mbus.network.exceptions.DefaultErrorHandler
import com.squareup.moshi.Moshi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dispatch.core.DefaultCoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager

class AndroidServiceFactory @Inject constructor(
   moshi: Provider<Moshi>,
   errorReporter: ErrorReporter,
   okHttpClient: Provider<OkHttpClient>,
   defaultCoroutineScope: DefaultCoroutineScope,
   defaultErrorHandler: DefaultErrorHandler,
   @BaseUrl
   baseUrl: String,
   private val cacheManager: GlobalOkHttpDiskCacheManager,
) : BaseServiceFactory(defaultCoroutineScope, moshi, okHttpClient, errorReporter, defaultErrorHandler, baseUrl) {
   override fun createCache(): Cache {
      return cacheManager.cache
   }
}
