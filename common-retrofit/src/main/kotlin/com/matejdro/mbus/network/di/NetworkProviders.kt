package com.matejdro.mbus.network.di

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import java.time.Duration

@ContributesTo(AppScope::class)
interface NetworkProviders {
   // Uncomment when adding adapters
   @Multibinds(allowEmpty = true)
   val serializationAdapters: Set<MoshiAdapter>

   @Provides
   @SingleIn(AppScope::class)
   fun provideMoshiInProviders(
      adapters: Set<@JvmSuppressWildcards com.matejdro.mbus.network.di.MoshiAdapter>,
   ): Moshi {
      return provideMoshi(adapters)
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideOkHttpClient(): OkHttpClient {
      if (Thread.currentThread().name == "main") {
         error("OkHttp should not be initialized on the main thread")
      }

      return prepareDefaultOkHttpClient().build()
   }

   companion object {
      fun provideMoshi(
         adapters: Set<@JvmSuppressWildcards MoshiAdapter>,
      ): Moshi {
         if (Thread.currentThread().name == "main") {
            error("Moshi should not be initialized on the main thread")
         }

         return Moshi.Builder().also { builder ->
            for (adapter in adapters) {
               if (adapter is JsonAdapter.Factory) {
                  builder.addLast(adapter)
               } else {
                  builder.addLast(adapter)
               }
            }
         }.build()
      }

      fun prepareDefaultOkHttpClient(): OkHttpClient.Builder {
         return OkHttpClient.Builder()
            .addInterceptor(BypassCacheInterceptor())
            .callTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
      }
   }
}

interface MoshiAdapter

private const val TIMEOUT_SECONDS = 30L
