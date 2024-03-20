package com.matejdro.mbus.stops.di

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.stops.StopsService
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@ContributesTo(ApplicationScope::class)
@Module
class StopsModule {
   @Provides
   fun provideStopsService(serviceFactory: ServiceFactory): StopsService = serviceFactory.create<StopsService>()
}
