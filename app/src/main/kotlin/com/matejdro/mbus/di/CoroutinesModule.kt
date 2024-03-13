package com.matejdro.mbus.di

import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dispatch.core.DefaultCoroutineScope
import dispatch.core.DispatcherProvider
import dispatch.core.IOCoroutineScope
import dispatch.core.MainImmediateCoroutineScope
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.reporting.ErrorReporter

@Suppress("unused")
@ContributesTo(ApplicationScope::class)
@Module
class CoroutinesModule {
   @Provides
   fun provideMainCoroutineScope(): MainImmediateCoroutineScope {
      return MainImmediateCoroutineScope(object : DispatcherProvider {})
   }

   @Provides
   fun provideDefaultCoroutineScope(): DefaultCoroutineScope {
      return DefaultCoroutineScope(object : DispatcherProvider {})
   }

   @Provides
   fun provideIOCoroutineScope(): IOCoroutineScope {
      return IOCoroutineScope(object : DispatcherProvider {})
   }

   @Provides
   fun provideDefaultCoroutineResourceManager(
      mainCoroutineScope: MainImmediateCoroutineScope,
      errorReporter: ErrorReporter,
   ): CoroutineResourceManager {
      return CoroutineResourceManager(mainCoroutineScope, errorReporter)
   }
}
