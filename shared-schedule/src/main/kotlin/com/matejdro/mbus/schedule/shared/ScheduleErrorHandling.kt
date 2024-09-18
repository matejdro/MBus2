package com.matejdro.mbus.schedule.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.mbus.schedule.exceptions.BrokenStationException
import com.matejdro.mbus.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.scheduleUserFriendlyMessage(
   hasExistingData: Boolean = false,
): String {
   return if (this is BrokenStationException) {
      stringResource(com.matejdro.mbus.schedule.shared.R.string.invalid_station_error)
   } else {
      commonUserFriendlyMessage(hasExistingData)
   }
}
