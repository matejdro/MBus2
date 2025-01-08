@file:Suppress("TooManyFunctions") // Previews. Waiting for https://github.com/detekt/detekt/issues/6516 to get merged

package com.matejdro.mbus.schedule.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import com.matejdro.mbus.navigation.keys.AddToFavouritesDialogScreenKey
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.shared.StopList
import com.matejdro.mbus.schedule.shared.TimePickerDialog
import com.matejdro.mbus.schedule.shared.scheduleUserFriendlyMessage
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.compose.result.registerResultReceiver
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.FakeAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider
import si.inova.kotlinova.navigation.screens.Screen
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import com.matejdro.mbus.schedule.shared.R as scheduleScharedR

@Stable
class StopScheduleScreen(
   private val viewModel: StopScheduleViewModel,
   private val timeProvider: TimeProvider,
   private val addToFavouritesDialogScreen: Screen<AddToFavouritesDialogScreenKey>,
) : Screen<StopScheduleScreenKey>() {
   @Composable
   override fun Content(key: StopScheduleScreenKey) {
      val state = viewModel.schedule.collectAsStateWithLifecycleAndBlinkingPrevention().value
      if (state != null) {
         val data = state.data
         var filterDialogShown by remember { mutableStateOf(false) }
         if (filterDialogShown && data != null) {
            FilterDialog(
               data.allLines,
               data.whitelistedLines,
               { filterDialogShown = false },
               {
                  filterDialogShown = false
                  viewModel.setFilter(it)
               }
            )
         }

         var timeDialogShown by remember { mutableStateOf(false) }
         if (timeDialogShown && data != null) {
            TimePickerDialog(
               ZonedDateTime.now(),
               { timeDialogShown = false },
               {
                  timeDialogShown = false
                  viewModel.changeDate(it.toLocalDateTime())
               }
            )
         }

         var favoriteDialogShown by remember { mutableStateOf(false) }
         if (favoriteDialogShown && data != null) {
            val closeDialogReceiver = registerResultReceiver<Unit> {
               favoriteDialogShown = false
            }

            addToFavouritesDialogScreen.Content(
               AddToFavouritesDialogScreenKey(key.stopId, data.stopName, closeDialogReceiver)
            )
         }

         ScheduleScreenContent(
            state,
            timeProvider,
            viewModel::loadNextPage,
            { filterDialogShown = true },
            { timeDialogShown = true },
            { favoriteDialogShown = true }
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
   data: Outcome<StopScheduleUiState>,
   timeProvider: TimeProvider,
   loadNextPage: () -> Unit,
   showFilter: () -> Unit,
   showTimePicker: () -> Unit,
   showAddFavoritePicker: () -> Unit,
) {
   Column {
      TopAppBar(
         title = { data.data?.stopName?.let { Text(it) } },
         actions = {
            Icon(
               painterResource(scheduleScharedR.drawable.ic_select_time),
               stringResource(scheduleScharedR.string.select_time),
               tint = if (data.data?.customTimeSet != true) {
                  LocalContentColor.current
               } else {
                  MaterialTheme.colorScheme.error
               },
               modifier = Modifier
                  .clickable(onClick = showTimePicker)
                  .padding(8.dp)
            )

            Icon(
               painterResource(scheduleScharedR.drawable.ic_filter),
               stringResource(scheduleScharedR.string.filter_lines),
               tint = if (data.data?.whitelistedLines.isNullOrEmpty()) {
                  LocalContentColor.current
               } else {
                  MaterialTheme.colorScheme.error
               },
               modifier = Modifier
                  .clickable(onClick = showFilter)
                  .padding(8.dp)
            )

            Icon(
               painterResource(com.matejdro.mbus.shared_resources.R.drawable.ic_favorite),
               stringResource(scheduleScharedR.string.add_to_favorites),
               modifier = Modifier
                  .clickable(onClick = showAddFavoritePicker)
                  .padding(8.dp)
            )
         }
      )

      TopLoading(data)
      TopError(data)

      val stopSchedule = data.data

      if (stopSchedule != null) {
         StopList(
            stopSchedule.arrivals,
            stopSchedule.stopImage,
            timeProvider,
            stopSchedule.hasAnyDataLeft,
            data is Outcome.Progress && data.style == LoadingStyle.ADDITIONAL_DATA,
            loadNextPage,
            Modifier.weight(1f)
         )
      }
   }
}

@Composable
private fun ColumnScope.TopError(data: Outcome<StopScheduleUiState>) {
   if (data is Outcome.Error) {
      Text(
         text = data.exception.scheduleUserFriendlyMessage(
            hasExistingData = !data.data?.arrivals.isNullOrEmpty()
         ),
         Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .padding(32.dp),
         color = MaterialTheme.colorScheme.error
      )
   }
}

@Composable
private fun ColumnScope.TopLoading(data: Outcome<StopScheduleUiState>) {
   if (data is Outcome.Progress && data.style != LoadingStyle.ADDITIONAL_DATA) {
      CircularProgressIndicator(
         Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .padding(32.dp)
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenSuccessPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Success(PREVIEW_FAKE_LIST),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenLoadingPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Progress(),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenRefreshLoadingPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Progress(PREVIEW_FAKE_LIST),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenErrorPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Error(NoNetworkException()),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenRefreshErrorPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Error(NoNetworkException(), PREVIEW_FAKE_LIST),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenRefreshLoadingMorePreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Progress(
            PREVIEW_FAKE_LIST.copy(
               hasAnyDataLeft = true,
               arrivals = PREVIEW_FAKE_LIST.arrivals.take(1)
            ),
            style = LoadingStyle.ADDITIONAL_DATA
         ),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenSuccessWithFilterAppliedPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Success(PREVIEW_FAKE_LIST.copy(whitelistedLines = setOf(2, 6, 18))),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenSuccessWithTimeSetApplied() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Success(PREVIEW_FAKE_LIST.copy(customTimeSet = true)),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
         {},
         {},
         {},
         {},
      )
   }
}

internal val PREVIEW_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
internal val PREVIEW_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
internal val PREVIEW_EXPECTED_LINE_18 = Line(18, "18", 0xFF00000.toInt())

val PREVIEW_FAKE_LIST = StopScheduleUiState(
   arrivals = listOf(
      Arrival(
         PREVIEW_EXPECTED_LINE_2,
         LocalDateTime.of(2024, 3, 30, 10, 0),
         "MB -> Mesto"
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_2,
         LocalDateTime.of(2024, 3, 31, 10, 20),
         "Mesto -> MB",
         6
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_6,
         LocalDateTime.of(2024, 4, 2, 11, 0),
         "MB -> Mesto",
         -3
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_18,
         LocalDateTime.of(2024, 4, 20, 11, 20),
         "MB -> Mesto",
         0
      ),
   ),
   stopName = "Forest 77",
   stopImage = "http://stopimage.com",
   stopDescription = "A stop in the forest",
   hasAnyDataLeft = false,
   allLines = listOf(PREVIEW_EXPECTED_LINE_2, PREVIEW_EXPECTED_LINE_6, PREVIEW_EXPECTED_LINE_18),
   whitelistedLines = emptySet(),
   selectedTime = ZonedDateTime.of(2024, 4, 20, 11, 20, 0, 0, ZoneId.of("UTC")),
   customTimeSet = false
)
