package com.matejdro.mbus.schedule.di

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.schedule.SchedulesService
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Module
@ContributesTo(ApplicationScope::class)
object SchedulesModule {
   @Provides
   fun provideSchedulesService(serviceFactory: ServiceFactory): SchedulesService = serviceFactory.create()
}
