package com.matejdro.mbus.network.services

import com.matejdro.mbus.network.converters.DateConverterFactory
import com.matejdro.mbus.network.exceptions.DefaultErrorHandler
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.callfactory.ErrorHandlingAdapterFactory
import si.inova.kotlinova.retrofit.callfactory.StaleWhileRevalidateCallAdapterFactory
import si.inova.kotlinova.retrofit.converter.LazyRetrofitConverterFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier

open class BaseServiceFactory @Inject constructor(
   private val coroutineScope: CoroutineScope,
   private val moshi: Provider<Moshi>,
   private val okHttpClient: Provider<OkHttpClient>,
   private val errorReporter: ErrorReporter,
   private val defaultErrorHandler: DefaultErrorHandler,
   @BaseUrl
   private val baseUrl: String,
) : ServiceFactory {
   override fun <S> create(klass: Class<S>, configuration: ServiceFactory.ServiceCreationScope.() -> Unit): S {
      val scope = ServiceFactory.ServiceCreationScope(defaultErrorHandler)
      configuration(scope)

      val updatedClient = lazy {
         okHttpClient.get().newBuilder()
            .apply {
               if (scope.cache) {
                  createCache()?.let { cache(it) }
               }
            }
            .apply {
               scope.okHttpCustomizer?.let { it() }
            }
            .build()
      }

      val moshiConverter = lazy {
         MoshiConverterFactory.create(moshi.get())
      }

      return Retrofit.Builder()
         .callFactory { updatedClient.value.newCall(it) }
         .baseUrl(baseUrl)
         .addConverterFactory(LazyRetrofitConverterFactory(moshiConverter))
         .addConverterFactory(DateConverterFactory)
         .addCallAdapterFactory(StaleWhileRevalidateCallAdapterFactory(scope.errorHandler, errorReporter))
         .addCallAdapterFactory(ErrorHandlingAdapterFactory(coroutineScope, scope.errorHandler))
         .build()
         .create(klass)
   }

   open fun createCache(): Cache? {
      return null
   }

   @Qualifier
   annotation class BaseUrl
}
