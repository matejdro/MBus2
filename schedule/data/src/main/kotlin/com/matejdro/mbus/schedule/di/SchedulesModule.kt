package com.matejdro.mbus.schedule.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.schedule.SchedulesService
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbLineQueries
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@ContributesTo(ApplicationScope::class)
object SchedulesModule {
   @Provides
   fun provideSchedulesService(serviceFactory: ServiceFactory): SchedulesService = serviceFactory.create()

   @Provides
   @Singleton
   fun provideLineQueries(driver: SqlDriver): DbLineQueries {
      return Database(driver).dbLineQueries
   }
}
