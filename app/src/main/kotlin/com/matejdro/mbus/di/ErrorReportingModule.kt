package com.matejdro.mbus.di

import com.matejdro.mbus.BuildConfig
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import si.inova.kotlinova.core.logging.logcat
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter

@Suppress("unused")
@ContributesTo(ApplicationScope::class)
@Module
class ErrorReportingModule {
   @Provides
   fun provideErrorReporter(): ErrorReporter {
      return ErrorReporter {
         if (it !is CauseException || it.shouldReport) {
            logcat { "Reporting $it to Firebase" }
            // TODO Substitute with error reporter here (Firebase?)
            it.printStackTrace()
         } else if (BuildConfig.DEBUG) {
            it.printStackTrace()
         }
      }
   }
}
