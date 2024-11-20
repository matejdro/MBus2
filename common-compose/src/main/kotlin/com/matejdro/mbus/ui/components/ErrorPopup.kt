package com.matejdro.mbus.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.matejdro.mbus.ui.R
import com.matejdro.mbus.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun ErrorPopup(
   exception: CauseException?,
   hasExistingData: Boolean,
   errorMessageProvider: @Composable CauseException.(hasExistingData: Boolean) -> String = { commonUserFriendlyMessage(it) },
) {
   var lastException: CauseException? by remember { mutableStateOf(null) }
   var showPopupMessage: Pair<CauseException, Boolean>? by remember { mutableStateOf(null) }

   showPopupMessage?.let { message ->
      AlertDialog(
         onDismissRequest = { showPopupMessage = null },
         title = { Text(stringResource(R.string.error)) },
         text = { Text(errorMessageProvider(message.first, message.second)) },
         confirmButton = { TextButton(onClick = { showPopupMessage = null }) { Text("OK") } }
      )
   }

   LaunchedEffect(exception, hasExistingData) {
      if (lastException !== exception) {
         lastException = exception
         if (exception != null) {
            showPopupMessage = exception to hasExistingData
         }
      }
   }
}
