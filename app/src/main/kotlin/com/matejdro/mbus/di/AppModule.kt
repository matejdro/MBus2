package com.matejdro.mbus.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.matejdro.mbus.Database
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dispatch.core.IOCoroutineScope
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatterImpl
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.DefaultAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider
import javax.inject.Singleton

@Suppress("unused")
@ContributesTo(ApplicationScope::class)
@Module
abstract class AppModule {
   @Binds
   abstract fun bindToContext(application: Application): Context

   @Binds
   abstract fun bindToTimeProvider(androidTimeProvider: AndroidTimeProvider): TimeProvider

   @Binds
   abstract fun bindToAndroidDateTimeFormatter(
      androidDateTimeFormatterImpl: AndroidDateTimeFormatterImpl,
   ): AndroidDateTimeFormatter

   @Module
   companion object {
      @Provides
      fun provideAndroidTimeProvider(): AndroidTimeProvider {
         return DefaultAndroidTimeProvider
      }

      @Provides
      @Singleton
      fun provideSqliteDriver(context: Context): SqlDriver {
         return AndroidSqliteDriver(Database.Schema, context, "database.db")
      }

      @Provides
      @Singleton
      fun provideDatastorePreferences(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<Preferences> {
         return PreferenceDataStoreFactory.create(scope = ioCoroutineScope) {
            context.preferencesDataStoreFile("login")
         }
      }
   }
}
