package com.matejdro.mbus.di

import com.matejdro.mbus.network.services.BaseServiceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@Suppress("unused")
@ContributesTo(AppScope::class)
interface NetworkUrlModuleProviders {
   @Provides
   @BaseServiceFactory.BaseUrl
   fun provideBaseUrl(): String {
      return "https://marprom-proxy.derp.si/OBA/"
   }
}
