package com.matejdro.mbus.favorites.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.navigation.keys.AddToFavouritesDialogScreenKey
import com.matejdro.mbus.ui.components.AlertDialogWithContent
import com.matejdro.mbus.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.mbus.ui.components.TextEntryAlertDialog
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.Screen

@ContributesScreenBinding
class AddToFavoritesDialogScreen(
   private val viewModel: AddToFavoritesDialogViewModel,
) : Screen<AddToFavouritesDialogScreenKey>() {
   @Composable
   override fun Content(key: AddToFavouritesDialogScreenKey) {
      LaunchedEffect(key.stopId) {
         viewModel.load(key.stopId)
      }

      val resultStore = LocalResultPassingStore.current
      val state = viewModel.data.collectAsStateWithLifecycleAndBlinkingPrevention()

      AddToFavoritesDialogScreenContent(
         key.stopName,
         state::value,
         { resultStore.sendResult(key.closeDialogReceiver, Unit) },
         viewModel::select,
         viewModel::addAndSelect
      )
   }
}

@Composable
private fun AddToFavoritesDialogScreenContent(
   stopName: String,
   data: () -> Outcome<AddToFavoritesDialogData>?,
   close: () -> Unit,
   select: (favoriteId: Long) -> Unit,
   addNew: (favoriteName: String) -> Unit,
) {
   var showNameEntryDialogScreen by remember { mutableStateOf(false) }

   AlertDialogWithContent(
      title = {
         // TODO Reuse R.strings.add_to_favorites in the future commit
         Text("Add to Favorites")
      },
      onDismissRequest = close,
      dismissButton = {
         TextButton(onClick = close) {
            Text(stringResource(android.R.string.cancel))
         }
      },
      confirmButton = {},
   ) {
      val outcome = data()

      LaunchedEffect(outcome?.data?.canClose) {
         if (outcome?.data?.canClose == true) {
            close()
         }
      }

      ProgressErrorSuccessScaffold(outcome) { data ->
         Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            for (favorite in data.favorites) {
               FavoriteRow(favorite.name) {
                  select(favorite.id)
               }
            }

            AddNewRow {
               showNameEntryDialogScreen = true
            }
         }
      }
   }

   if (showNameEntryDialogScreen) {
      TextEntryAlertDialog(
         { Text(stringResource(R.string.add_new_favorite)) },
         stopName,
         { showNameEntryDialogScreen = false },
         { addNew(it) },
      )
   }
}

@Composable
private fun FavoriteRow(
   name: String,
   click: () -> Unit,
) {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .clickable(onClick = click)
         .padding(16.dp)
   ) {
      Text(name)
   }
}

@Composable
private fun AddNewRow(
   click: () -> Unit,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
         .fillMaxWidth()
         .clickable(onClick = click)
         .padding(16.dp)
   ) {
      Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.padding(end = 8.dp))
      Text(stringResource(R.string.add_new_favorite))
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun AddToFavoritesDialogScreenContentSuccessPreview() {
   PreviewTheme {
      AddToFavoritesDialogScreenContent(
         "",
         {
            Outcome.Success(
               AddToFavoritesDialogData(
                  false,
                  listOf(
                     Favorite(1, "Fav 1", emptyList()),
                     Favorite(2, "Fav 2", emptyList()),
                     Favorite(3, "Fav 3", emptyList()),
                     Favorite(4, "Fav 4", emptyList()),
                     Favorite(4, "Fav 4", emptyList())
                  )
               )
            )
         },
         {},
         {},
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun AddToFavoritesDialogScreenContentErrorPreview() {
   PreviewTheme {
      AddToFavoritesDialogScreenContent(
         "",
         {
            Outcome.Error(UnknownCauseException())
         },
         {},
         {},
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun AddToFavoritesDialogScreenContentProgressPreview() {
   PreviewTheme {
      AddToFavoritesDialogScreenContent(
         "",
         {
            Outcome.Progress()
         },
         {},
         {},
         {},
      )
   }
}
