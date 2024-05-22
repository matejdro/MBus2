package com.matejdro.mbus.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.matejdro.mbus.BuildConfig
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesBinding
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
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
