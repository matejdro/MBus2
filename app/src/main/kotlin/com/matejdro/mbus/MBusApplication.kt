package com.matejdro.mbus

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.os.strictmode.Violation
import androidx.core.content.ContextCompat
import com.matejdro.mbus.di.ApplicationComponent
import com.matejdro.mbus.di.DaggerMainApplicationComponent
import dispatch.core.DefaultDispatcherProvider
import si.inova.kotlinova.core.dispatchers.AccessCallbackDispatcherProvider
import si.inova.kotlinova.core.logging.AndroidLogcatLogger
import si.inova.kotlinova.core.logging.LogPriority
import si.inova.kotlinova.core.reporting.ErrorReporter
import javax.inject.Inject
import javax.inject.Provider

open class MBusApplication : Application() {
   @Inject
   lateinit var errorReporter: Provider<ErrorReporter>

   init {
      if (BuildConfig.DEBUG) {
         // Enable better coroutine stack traces on debug builds
         // this slows down coroutines, so it should not be enabled on release
         // using init instead of onCreate ensures that this is started before any content providers
         System.setProperty("kotlinx.coroutines.debug", "on")
      }
   }

   override fun onCreate() {
      super.onCreate()

      AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)

      enableStrictMode()

      DefaultDispatcherProvider.set(
         AccessCallbackDispatcherProvider(DefaultDispatcherProvider.get()) {
            if (BuildConfig.DEBUG) {
               error("Dispatchers not provided via coroutine scope.")
            }
         }
      )
   }

   private fun enableStrictMode() {
      // Also check on staging release build, if applicable
      // penaltyListener only supports P and newer, so we are forced to only enable StrictMode on those devices
      if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
         return
      }

      StrictMode.setVmPolicy(
         VmPolicy.Builder()
            .detectActivityLeaks()
            .detectFileUriExposure()
            .detectContentUriWithoutPermission()
            .run {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  detectImplicitDirectBoot()
                     .detectCredentialProtectedWhileLocked()
               } else {
                  this
               }
            }
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyListener(ContextCompat.getMainExecutor(this@MBusApplication)) { violation ->
               reportStrictModePenalty(violation)
            }
            .build()
      )

      StrictMode.setThreadPolicy(
         StrictMode.ThreadPolicy.Builder()
            .detectCustomSlowCalls()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectResourceMismatches()
            .detectUnbufferedIo()
            .penaltyListener(ContextCompat.getMainExecutor(this)) { e ->
               if (BuildConfig.DEBUG) {
                  throw e
               } else {
                  errorReporter.get().report(e)
               }
            }
            .build()
      )
   }

   private fun reportStrictModePenalty(violation: Violation) {
      val e = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
         violation
      } else {
         IllegalStateException("Strict mode violation: $violation")
      }

      if (
         e.cause == null &&
         (
            STRICT_MODE_EXCLUSIONS.any {
               e.toString().contains(it)
            } ||
               e.stackTrace.any { stackTraceElement ->
                  STRICT_MODE_EXCLUSIONS.any {
                     stackTraceElement.className.contains(it)
                  }
               }
            )
      ) {
         return
      }

      if (BuildConfig.DEBUG) {
         throw e
      } else {
         errorReporter.get().report(e)
      }
   }

   open val applicationComponent: ApplicationComponent by lazy {
      DaggerMainApplicationComponent.factory().create(this)
   }
}

private val STRICT_MODE_EXCLUSIONS = listOf(
   "UnixSecureDirectoryStream", // https://issuetracker.google.com/issues/270704908
   "UnixDirectoryStream", // https://issuetracker.google.com/issues/270704908,
   "coil.RealImageLoader", // https://github.com/coil-kt/coil/issues/1878
   "InsetsSourceControl", // https://issuetracker.google.com/issues/307473789
)
