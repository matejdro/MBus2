package com.matejdro.mbus.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.ui.debugging.PreviewTheme

@Composable
fun TextEntryAlertDialog(
   title: @Composable () -> Unit,
   originalText: String,
   onCancel: () -> Unit,
   onConfirm: (newText: String) -> Unit,
   modifier: Modifier = Modifier,
) {
   var currentText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(
         TextFieldValue(originalText, TextRange(0, originalText.length))
      )
   }

   AlertDialogWithContent(
      onDismissRequest = onCancel,
      confirmButton = {
         TextButton(onClick = { onConfirm(currentText.text) }) { Text(stringResource(android.R.string.ok)) }
      },
      modifier = modifier,
      dismissButton = {
         TextButton(onClick = onCancel) { Text(stringResource(android.R.string.cancel)) }
      },
      title = title
   ) {
      val focusRequester = FocusRequester()
      TextField(
         currentText,
         onValueChange = { currentText = it },
         Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
         singleLine = true,
         keyboardActions = KeyboardActions(
            onDone = { onConfirm(currentText.text) }
         ),
         keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
         )
      )

      LaunchedEffect(Unit) {
         focusRequester.requestFocus()
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components")
internal fun TextEntryAlertDialogPreview() {
   PreviewTheme(fill = false) {
      TextEntryAlertDialog(
         { Text("Enter some text") },
         "ABC",
         {},
         {}
      )
   }
}
