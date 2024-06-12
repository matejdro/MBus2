package com.matejdro.mbus.favorites.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.favorites.model.LineStop
import com.matejdro.mbus.schedule.shared.LineLabel
import com.matejdro.mbus.ui.components.AlertDialogWithContent
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import com.matejdro.mbus.schedule.shared.R as scheduleSharedR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineStopFilterDialog(
   allLines: List<LineStop>,
   selectedLines: Set<LineStop>,
   onCancel: () -> Unit,
   onSubmit: (Set<LineStop>) -> Unit,
   modifier: Modifier = Modifier,
) {
   val selectedLinesInDialog = remember {
      val initialSelection = if (selectedLines.isEmpty()) allLines.toTypedArray() else selectedLines.toTypedArray()
      mutableStateListOf(*initialSelection)
   }

   AlertDialogWithContent(
      onDismissRequest = onCancel,
      title = { Text(stringResource(scheduleSharedR.string.line_filter)) },
      confirmButton = {
         TextButton(onClick = { onSubmit(selectedLinesInDialog.toSet()) }) {
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
      Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
         for (line in allLines) {
            FilterRow(selectedLinesInDialog, line)
         }
      }
   }
}

@Composable
private fun FilterRow(
   selectedLinesInDialog: SnapshotStateList<LineStop>,
   lineStop: LineStop,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .fillMaxWidth()
         .clickable {
            if (!selectedLinesInDialog.contains(lineStop)) {
               selectedLinesInDialog.add(lineStop)
            } else {
               selectedLinesInDialog.remove(lineStop)
            }
         }
   ) {
      Checkbox(selectedLinesInDialog.contains(lineStop), onCheckedChange = {
         if (it) {
            selectedLinesInDialog.add(lineStop)
         } else {
            selectedLinesInDialog.remove(lineStop)
         }
      })

      LineLabel(lineStop.line)
      Text(lineStop.stop.name, Modifier.padding(start = 16.dp))
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenLineStopFilterDialogPreview() {
   PreviewTheme() {
      LineStopFilterDialog(
         listOf(
            LineStop(PREVIEW_EXPECTED_LINE_2, PREVIEW_STOP_7),
            LineStop(PREVIEW_EXPECTED_LINE_2, PREVIEW_STOP_8),
            LineStop(PREVIEW_EXPECTED_LINE_6, PREVIEW_STOP_8)
         ),
         setOf(
            LineStop(PREVIEW_EXPECTED_LINE_2, PREVIEW_STOP_8),
         ),
         {},
         {}
      )
   }
}
