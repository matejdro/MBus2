package com.matejdro.mbus.favorites.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@ContributesTo(ApplicationScope::class)
object FavoritesModule {
   @Provides
   @Singleton
   fun provideFavoriteQueries(driver: SqlDriver): DbFavoriteQueries {
      return Database(driver).dbFavoriteQueries
   }
}
