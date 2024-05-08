package com.matejdro.mbus.schedule.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.ui.components.AlertDialogWithContent
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
   selectedTime: ZonedDateTime,
   onCancel: () -> Unit,
   onSubmit: (ZonedDateTime) -> Unit,
   modifier: Modifier = Modifier,
) {
   val timePickerState =
      rememberTimePickerState(initialHour = selectedTime.hour, initialMinute = selectedTime.minute, is24Hour = true)
   val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = selectedTime.toInstant().toEpochMilli(),
      initialDisplayMode = DisplayMode.Picker
   )

   AlertDialogWithContent(
      onDismissRequest = onCancel,
      title = { Text(stringResource(R.string.select_time)) },
      confirmButton = {
         TextButton(onClick = {
            val selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton

            onSubmit(
               Instant.ofEpochMilli(selectedDateMillis)
                  .atZone(ZoneId.systemDefault())
                  .withHour(timePickerState.hour)
                  .withMinute(timePickerState.minute)
            )
         }) {
            Text(stringResource(android.R.string.ok))
         }
      },
      dismissButton = {
         TextButton(onClick = onCancel) {
            Text(stringResource(android.R.string.cancel))
         }
      },
      modifier = modifier
   ) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
         TimeInput(
            timePickerState,
         )

         DatePicker(
            datePickerState,
            showModeToggle = false,
            headline = null,
            title = null
         )
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun TimePickerFilterDialogPreview() {
   PreviewTheme() {
      TimePickerDialog(
         ZonedDateTime.of(2024, 2, 10, 15, 30, 0, 0, ZoneId.of("UTC")),
         {},
         {}
      )
   }
}
