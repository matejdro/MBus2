package com.matejdro.mbus.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.matejdro.mbus.BuildConfig
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesBinding
import si.inova.kotlinova.core.logging.logcat
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@ContributesBinding(ApplicationScope::class)
class FirebaseErrorReporter @Inject constructor() : ErrorReporter {
   private val crashlytics = FirebaseCrashlytics.getInstance()

   override fun report(throwable: Throwable) {
      if (throwable is CancellationException) {
         report(Exception("Got cancellation exception", throwable))
         return
      }

      if (throwable !is CauseException || throwable.shouldReport) {
         logcat { "Reporting $throwable to Firebase" }
         crashlytics.recordException(throwable)
      }

      if (BuildConfig.DEBUG) {
         throwable.printStackTrace()
      }
   }
}
