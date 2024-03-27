package com.matejdro.mbus.stops.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbStopQueries
import com.matejdro.mbus.stops.StopsService
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@ContributesTo(ApplicationScope::class)
@Module
object StopsModule {
   @Provides
   fun provideStopsService(serviceFactory: ServiceFactory): StopsService = serviceFactory.create<StopsService>()

   @Provides
   @Singleton
   fun provideStopQueries(driver: SqlDriver): DbStopQueries {
      return Database(driver).dbStopQueries
   }
}
