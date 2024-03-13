package com.matejdro.mbus.ui.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.mbus.ui.R
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.commonUserFriendlyMessage(): String {
   return if (this is NoNetworkException) {
      stringResource(R.string.error_no_network)
   } else {
      stringResource(R.string.error_unknown)
   }
}
