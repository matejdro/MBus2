package com.matejdro.mbus.favorites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.ui.components.AlertDialogWithContent
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFavoriteDialog(
   name: String,
   includedStops: List<StopInfo>,
   onCancel: () -> Unit,
   onSubmit: (newName: String, newStops: List<StopInfo>) -> Unit,
   onDelete: () -> Unit,
   modifier: Modifier = Modifier,
) {
   var includedStopsInDialog by remember { mutableStateOf(includedStops) }
   var newName by remember { mutableStateOf(name) }

   AlertDialogWithContent(
      onDismissRequest = onCancel,
      title = { Text(stringResource(R.string.edit_favorite)) },
      confirmButton = {
         TextButton(
            onClick = {
               if (includedStopsInDialog.isEmpty()) {
                  onDelete()
               } else {
                  onSubmit(newName, includedStops - includedStopsInDialog)
               }
            },
            enabled = newName.isNotBlank()
         ) {
            Text(stringResource(android.R.string.ok))
         }
      },
      dismissButton = {
         TextButton(onClick = onCancel) {
            Text(stringResource(android.R.string.cancel))
         }
      },
      neutralButton = {
         TextButton(onClick = onDelete) {
            Text(stringResource(R.string.delete_all))
         }
      },
      modifier = modifier
   ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
         TextField(newName, onValueChange = { newName = it }, modifier = Modifier.fillMaxWidth())

         includedStopsInDialog.forEachIndexed { index, stop ->
            key(stop.id) {
               StopRow(stop) { removedId -> includedStopsInDialog = includedStopsInDialog.filter { it.id != removedId } }
            }

            if (index != includedStopsInDialog.size - 1) {
               HorizontalDivider()
            }
         }
      }
   }
}

@Composable
private fun StopRow(
   stop: StopInfo,
   delete: (Int) -> Unit,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .fillMaxWidth()
   ) {
      Text(stop.name)
      Spacer(Modifier.weight(1f))

      IconButton(onClick = { delete(stop.id) }) {
         Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun EditFavoriteDialogPreview() {
   PreviewTheme() {
      EditFavoriteDialog(
         "A favorite",
         listOf(
            StopInfo(1, "Stop 1"),
            StopInfo(2, "Stop 2"),
            StopInfo(2, "Stop 3"),
         ),
         {},
         { _, _ -> },
         {}
      )
   }
}
