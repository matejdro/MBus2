package com.matejdro.mbus.stops.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbStopQueries
import com.matejdro.mbus.stops.StopsService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface StopsProviders {
   @Provides
   fun provideStopsService(serviceFactory: ServiceFactory): StopsService = serviceFactory.create<StopsService>()

   @Provides
   @SingleIn(AppScope::class)
   fun provideStopQueriesInProvider(driver: SqlDriver): DbStopQueries {
      return provideStopQueries(driver)
   }

   companion object {
      fun provideStopQueries(driver: SqlDriver): DbStopQueries {
         return Database(driver).dbStopQueries
      }
   }
}
