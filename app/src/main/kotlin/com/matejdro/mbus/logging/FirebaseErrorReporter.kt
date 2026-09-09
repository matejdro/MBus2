package com.matejdro.mbus.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.matejdro.mbus.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter

@ContributesBinding(AppScope::class)
class FirebaseErrorReporter @Inject constructor() : ErrorReporter {
   private val crashlytics = FirebaseCrashlytics.getInstance()

   override fun report(throwable: Throwable) {
      if (throwable !is CauseException) {
         report(UnknownCauseException("Got reported non-cause exception", throwable))
         return
      }
      if (BuildConfig.DEBUG) {
         throwable.printStackTrace()
      } else if (throwable.shouldReport) {
         crashlytics.recordException(throwable)
      }
   }
}
