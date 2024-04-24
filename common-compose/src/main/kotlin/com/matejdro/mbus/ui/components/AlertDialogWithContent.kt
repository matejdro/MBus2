package com.matejdro.mbus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.ui.debugging.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogWithContent(
   onDismissRequest: () -> Unit,
   confirmButton: @Composable () -> Unit,
   modifier: Modifier = Modifier,
   dismissButton: @Composable (() -> Unit) = { },
   title: @Composable (() -> Unit)? = null,
   content: @Composable () -> Unit,
) {
   BasicAlertDialog(
      onDismissRequest = onDismissRequest,
      modifier = modifier,
      content = {
         Surface(
            shape = MaterialTheme.shapes.large,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
         ) {
            Column(
               modifier = Modifier
                  .padding(24.dp)
                  .verticalScroll(rememberScrollState())
            ) {
               title?.let {
                  CompositionLocalProvider(
                     LocalTextStyle provides MaterialTheme.typography.headlineSmall
                  ) {
                     it()
                  }

                  Spacer(Modifier.height(16.dp))
               }

               content()

               Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  dismissButton()
                  confirmButton()
               }
            }
         }
      }
   )
}

@Preview
@Composable
@ShowkaseComposable(group = "Components")
internal fun AlertDialogWithContentPreview() {
   PreviewTheme(fill = false) {
      AlertDialogWithContent(
         onDismissRequest = {},
         confirmButton = { TextButton(onClick = {}) { Text("OK") } },
         dismissButton = { TextButton(onClick = {}) { Text("Cancel") } },
         title = { Text("Title") },
      ) {
         Button(onClick = {}) {
            Text("A button inside dialog")
         }
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components")
internal fun AlertDialogWithContentWithoutTitleAndCancelButtonPreview() {
   PreviewTheme(fill = false) {
      AlertDialogWithContent(
         onDismissRequest = {},
         confirmButton = { TextButton(onClick = {}) { Text("OK") } },
      ) {
         Button(onClick = {}) {
            Text("A button inside dialog")
         }
      }
   }
}
