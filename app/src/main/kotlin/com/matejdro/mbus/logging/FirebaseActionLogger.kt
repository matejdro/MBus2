package com.matejdro.mbus.logging

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.matejdro.mbus.common.logging.ActionLogger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
class FirebaseActionLogger @Inject constructor() : ActionLogger {
   override fun logAction(text: () -> String) {
      Firebase.crashlytics.log(text())
   }
}
