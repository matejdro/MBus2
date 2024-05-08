package com.matejdro.mbus.schedule.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.ui.components.AlertDialogWithContent
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
   allLines: List<Line>,
   selectedLines: Set<Int>,
   onCancel: () -> Unit,
   onSubmit: (Set<Int>) -> Unit,
   modifier: Modifier = Modifier,
) {
   val selectedLinesInDialog = remember {
      val initialSelection = if (selectedLines.isEmpty()) allLines.map { it.id }.toTypedArray() else selectedLines.toTypedArray()
      mutableStateListOf(*initialSelection)
   }

   AlertDialogWithContent(
      onDismissRequest = onCancel,
      title = { Text(stringResource(R.string.line_filter)) },
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
   selectedLinesInDialog: SnapshotStateList<Int>,
   line: Line,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .fillMaxWidth()
         .clickable {
            if (!selectedLinesInDialog.contains(line.id)) {
               selectedLinesInDialog.add(line.id)
            } else {
               selectedLinesInDialog.remove(line.id)
            }
         }
   ) {
      Checkbox(selectedLinesInDialog.contains(line.id), onCheckedChange = {
         if (it) {
            selectedLinesInDialog.add(line.id)
         } else {
            selectedLinesInDialog.remove(line.id)
         }
      })
      LineLabel(line)
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenFilterDialogPreview() {
   PreviewTheme() {
      FilterDialog(
         listOf(PREVIEW_EXPECTED_LINE_2, PREVIEW_EXPECTED_LINE_6, PREVIEW_EXPECTED_LINE_18),
         setOf(6),
         {},
         {}
      )
   }
}

private val PREVIEW_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val PREVIEW_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
private val PREVIEW_EXPECTED_LINE_18 = Line(18, "18", 0xFF00000.toInt())
