package com.matejdro.mbus.network.di

import com.appmattus.certificatetransparency.cache.DiskCache
import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import javax.inject.Singleton

@Module
@ContributesTo(ApplicationScope::class)
abstract class NetworkModule {
   @Multibinds
   abstract fun multibindsMoshiAdapters(): Set<@JvmSuppressWildcards MoshiAdapter>

   companion object {
      @Provides
      @Singleton
      fun provideMoshi(
         adapters: Set<@JvmSuppressWildcards MoshiAdapter>,
      ): Moshi {
         if (Thread.currentThread().name == "main") {
            error("Moshi should not be initialized on the main thread")
         }

         return Moshi.Builder().also {
            for (adapter in adapters) {
               if (adapter is JsonAdapter.Factory) {
                  it.addLast(adapter)
               } else {
                  it.addLast(adapter)
               }
            }
         }.build()
      }

      @Provides
      @Singleton
      fun provideOkHttpClient(
         certificateTransparencyDiskCache: DiskCache?,
      ): OkHttpClient {
         if (Thread.currentThread().name == "main") {
            error("OkHttp should not be initialized on the main thread")
         }

         return prepareDefaultOkHttpClient(certificateTransparencyDiskCache).build()
      }

      fun prepareDefaultOkHttpClient(certificateTransparencyDiskCache: DiskCache? = null): OkHttpClient.Builder {
         return OkHttpClient.Builder()
            .addInterceptor(BypassCacheInterceptor())
            .addNetworkInterceptor(
               certificateTransparencyInterceptor {
                  diskCache = certificateTransparencyDiskCache
               }
            )
      }
   }
}

interface MoshiAdapter
