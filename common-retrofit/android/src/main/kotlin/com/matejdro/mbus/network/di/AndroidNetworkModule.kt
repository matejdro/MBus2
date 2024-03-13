package com.matejdro.mbus.network.di

import android.content.Context
import com.appmattus.certificatetransparency.cache.AndroidDiskCache
import com.appmattus.certificatetransparency.cache.DiskCache
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.AndroidServiceFactory
import com.matejdro.mbus.network.services.ServiceFactory
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager
import javax.inject.Singleton

@Module
@ContributesTo(ApplicationScope::class)
abstract class AndroidNetworkModule {
   @Binds
   abstract fun bindToServiceFactory(androidServiceFactory: AndroidServiceFactory): ServiceFactory

   companion object {
      @Provides
      fun provideDiskCacheManager(
         context: Context,
         errorReporter: ErrorReporter,
      ): GlobalOkHttpDiskCacheManager {
         return GlobalOkHttpDiskCacheManager(context, errorReporter)
      }

      @Provides
      @Singleton
      fun provideCertificateTransparencyDiskCache(context: Context): DiskCache {
         return AndroidDiskCache(context)
      }
   }
}
