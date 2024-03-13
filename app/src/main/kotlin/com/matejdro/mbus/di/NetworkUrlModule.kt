package com.matejdro.mbus.di

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.BaseServiceFactory
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Suppress("unused")
@ContributesTo(ApplicationScope::class)
@Module
class NetworkUrlModule {
   @Provides
   @BaseServiceFactory.BaseUrl
   fun provideBaseUrl(): String {
      // TODO Bus API
      error("Retrofit base url not provided")
   }
}
