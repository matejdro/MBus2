package com.matejdro.mbus.di

import android.app.Application
import android.content.Context
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatterImpl
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.DefaultAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider

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
   }
}
