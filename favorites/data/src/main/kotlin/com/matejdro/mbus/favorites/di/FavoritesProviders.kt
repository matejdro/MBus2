package com.matejdro.mbus.favorites.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface FavoritesProviders {
   @Provides
   @SingleIn(AppScope::class)
   fun provideFavoriteQueries(driver: SqlDriver): DbFavoriteQueries {
      return Database(driver).dbFavoriteQueries
   }
}
