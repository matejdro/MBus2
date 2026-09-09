package com.matejdro.mbus.network.di

import android.content.Context
import com.matejdro.mbus.network.services.AndroidServiceFactory
import com.matejdro.mbus.network.services.ServiceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager

@ContributesTo(AppScope::class)
interface AndroidNetworkProviders {
   @Binds
   abstract fun bindToServiceFactory(androidServiceFactory: AndroidServiceFactory): ServiceFactory

   @Provides
   fun provideDiskCacheManager(
      context: Context,
      errorReporter: ErrorReporter,
   ): GlobalOkHttpDiskCacheManager {
      return GlobalOkHttpDiskCacheManager(context, errorReporter)
   }
}
