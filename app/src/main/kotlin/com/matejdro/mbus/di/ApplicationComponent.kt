package com.matejdro.mbus.di

import android.app.Application
import com.matejdro.mbus.MainActivity
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import javax.inject.Singleton

@MergeComponent(ApplicationScope::class)
@MergeComponent(OuterNavigationScope::class)
@Singleton
interface MainApplicationComponent : ApplicationComponent {
   @Component.Factory
   interface Factory {
      fun create(
         @BindsInstance
         application: Application,
      ): MainApplicationComponent
   }
}

interface ApplicationComponent {
   fun inject(mainActivity: MainActivity)
}
