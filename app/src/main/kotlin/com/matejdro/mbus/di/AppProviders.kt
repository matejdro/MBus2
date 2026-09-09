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
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatterImpl
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.DefaultAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider

@Suppress("unused")
@ContributesTo(AppScope::class)
interface AppProviders {
   @Binds
   fun bindToContext(application: Application): Context

   @Binds
   fun bindToTimeProvider(androidTimeProvider: AndroidTimeProvider): TimeProvider

   @Provides
   fun provideAndroidDateTimeFormatter(
      context: Context,
      errorReporter: ErrorReporter,
   ): AndroidDateTimeFormatter = AndroidDateTimeFormatterImpl(context, errorReporter)

   @Provides
   fun provideAndroidTimeProvider(): AndroidTimeProvider {
      return DefaultAndroidTimeProvider
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideSqliteDriver(context: Context): SqlDriver {
      return AndroidSqliteDriver(Database.Schema, context, "database.db")
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideDatastorePreferences(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<Preferences> {
      return PreferenceDataStoreFactory.create(scope = ioCoroutineScope) {
         context.preferencesDataStoreFile("login")
      }
   }
}
