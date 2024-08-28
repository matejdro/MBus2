package com.matejdro.mbus.favorites.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.navigation.keys.FavoriteListScreenKey
import com.matejdro.mbus.navigation.keys.FavoriteScheduleScreenKey
import com.matejdro.mbus.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.instructions.replaceTopWith
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.Screen

class FavoriteListScreen(
   private val viewModel: FavoriteListViewModel,
   private val navigator: Navigator,
) : Screen<FavoriteListScreenKey>() {
   @Composable
   override fun Content(key: FavoriteListScreenKey) {
      val state = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention()

      FavoriteListScreenContent(state.value) {
         navigator.replaceTopWith(FavoriteScheduleScreenKey(it))
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteListScreenContent(data: Outcome<List<Favorite>>?, selectFavorite: (id: Long) -> Unit) {
   Column {
      TopAppBar(title = { Text(stringResource(com.matejdro.mbus.shared_resources.R.string.favorites)) })

      ProgressErrorSuccessScaffold(
         data,
         Modifier
            .fillMaxWidth()
            .weight(1f)
      ) { items ->
         Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize()) {
               itemsWithDivider(items) {
                  Text(
                     it.name,
                     Modifier
                        .clickable { selectFavorite(it.id) }
                        .padding(24.dp)
                        .fillMaxWidth()
                  )
               }
            }

            if (data is Outcome.Success && items.isEmpty()) {
               Text(stringResource(R.string.no_favorites_placeholder), Modifier.align(Alignment.Center))
            }
         }
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun SuccessPreview() {
   PreviewTheme {
      FavoriteListScreenContent(
         Outcome.Success(
            listOf(
               Favorite(
                  1,
                  "Favorite a",
                  emptyList()
               ),
               Favorite(
                  2,
                  "Favorite b",
                  emptyList()
               ),
               Favorite(
                  3,
                  "Favorite c",
                  emptyList()
               ),
               Favorite(
                  4,
                  "Favorite d",
                  emptyList()
               )
            )
         ),
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun EmptyPreview() {
   PreviewTheme {
      FavoriteListScreenContent(
         Outcome.Success(
            emptyList()
         ),
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ErrorPreview() {
   PreviewTheme {
      FavoriteListScreenContent(
         Outcome.Error(UnknownCauseException()),
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun LoadingPreview() {
   PreviewTheme {
      FavoriteListScreenContent(
         Outcome.Progress(),
         {},
      )
   }
}
