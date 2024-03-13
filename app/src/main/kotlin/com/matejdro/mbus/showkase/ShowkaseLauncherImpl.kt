package com.matejdro.mbus.showkase

import android.content.Context
import com.airbnb.android.showkase.annotation.ShowkaseRoot
import com.airbnb.android.showkase.annotation.ShowkaseRootModule
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.ui.showkase.ShowkaseLauncher
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class ShowkaseLauncherImpl @Inject constructor() : ShowkaseLauncher {
   override fun launch(context: Context) {
      // TODO uncomment this when you have at least one preview marked with @ShowkaseComposable
      // context.startActivity(Showkase.getBrowserIntent(context))

      // To enable showkase, you need to put a button somewhere that is only visible on debug builds and make button
      // call ShowkaseLauncher.launch() when clicked.
   }
}

@ShowkaseRoot
class MyRootModule : ShowkaseRootModule
