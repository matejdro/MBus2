package com.matejdro.mbus.logging

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.common.logging.ActionLogger
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class FirebaseActionLogger @Inject constructor() : ActionLogger {
   override fun logAction(text: () -> String) {
      Firebase.crashlytics.log(text())
   }
}
