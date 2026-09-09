package com.matejdro.mbus.schedule.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.network.services.ServiceFactory
import com.matejdro.mbus.network.services.create
import com.matejdro.mbus.schedule.SchedulesService
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbArrivalQueries
import com.matejdro.mbus.sqldelight.generated.DbLineQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface SchedulesProviders {
   @Provides
   fun provideSchedulesService(serviceFactory: ServiceFactory): SchedulesService = serviceFactory.create()

   @Provides
   @SingleIn(AppScope::class)
   fun provideLineQueriesInModule(driver: SqlDriver): DbLineQueries {
      return provideLineQueries(driver)
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideArrivalQueriesInModule(driver: SqlDriver): DbArrivalQueries {
      return provideArrivalQueries(driver)
   }

   companion object {
      fun provideLineQueries(driver: SqlDriver): DbLineQueries {
         return Database(driver).dbLineQueries
      }

      fun provideArrivalQueries(driver: SqlDriver): DbArrivalQueries {
         return Database(driver).dbArrivalQueries
      }
   }
}
