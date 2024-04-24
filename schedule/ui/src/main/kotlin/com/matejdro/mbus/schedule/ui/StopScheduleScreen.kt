@file:Suppress("TooManyFunctions") // Previews. Waiting for https://github.com/detekt/detekt/issues/6516 to get merged

package com.matejdro.mbus.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.R
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import com.matejdro.mbus.ui.errors.commonUserFriendlyMessage
import com.matejdro.mbus.ui.lists.DetectScrolledToBottom
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.compose.time.LocalDateFormatter
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
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

class StopScheduleScreen(
   private val viewModel: StopScheduleViewModel,
   private val timeProvider: TimeProvider,
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

         ScheduleScreenContent(
            state,
            timeProvider,
            viewModel::loadNextPage,
            { filterDialogShown = true },
            { timeDialogShown = true }
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
   data: Outcome<ScheduleUiState>,
   timeProvider: TimeProvider,
   loadNextPage: () -> Unit,
   showFilter: () -> Unit,
   showTimePicker: () -> Unit,
) {
   Column {
      TopAppBar(
         title = { data.data?.stopName?.let { Text(it) } },
         actions = {
            Icon(
               painterResource(R.drawable.ic_select_time),
               stringResource(R.string.select_time),
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
               painterResource(R.drawable.ic_filter),
               stringResource(R.string.filter_lines),
               tint = if (data.data?.whitelistedLines.isNullOrEmpty()) {
                  LocalContentColor.current
               } else {
                  MaterialTheme.colorScheme.error
               },
               modifier = Modifier
                  .clickable(onClick = showFilter)
                  .padding(8.dp)
            )
         }
      )

      TopLoading(data)
      TopError(data)

      val stopSchedule = data.data

      if (stopSchedule != null) {
         val state = rememberLazyListState()

         state.DetectScrolledToBottom(loadNextPage)

         LazyColumn(Modifier.weight(1f), state) {
            stopImageItem(stopSchedule)

            itemsWithDivider(stopSchedule.arrivals) {
               ScheduleItem(it, timeProvider)
            }

            bottomLoading(stopSchedule, data)
         }
      }
   }
}

@Composable
private fun ColumnScope.TopError(data: Outcome<ScheduleUiState>) {
   if (data is Outcome.Error) {
      Text(
         text = data.exception.commonUserFriendlyMessage(
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
private fun ColumnScope.TopLoading(data: Outcome<ScheduleUiState>) {
   if (data is Outcome.Progress && data.style != LoadingStyle.ADDITIONAL_DATA) {
      CircularProgressIndicator(
         Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .padding(32.dp)
      )
   }
}

private fun LazyListScope.stopImageItem(stopSchedule: ScheduleUiState) {
   stopSchedule.stopImage?.let {
      item {
         AsyncImage(
            model = it,
            modifier = Modifier
               .fillMaxWidth()
               .height(200.dp),
            contentScale = ContentScale.Crop,
            contentDescription = null
         )
      }
   }
}

@Composable
private fun ScheduleItem(it: Arrival, timeProvider: TimeProvider) {
   val delayMin = it.liveDelayMin

   Row(
      Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
   ) {
      LineLabel(it.line)

      Column(Modifier.weight(1f)) {
         Row {
            if (delayMin != null && delayMin != 0) {
               Text(
                  fontSize = 20.sp,
                  text = it.timeText(timeProvider.currentLocalDate(), plusMinutes = -delayMin),
                  textDecoration = TextDecoration.LineThrough,
                  modifier = Modifier.padding(end = 4.dp)
               )
            }
            Text(
               fontSize = 20.sp,
               text = it.timeText(timeProvider.currentLocalDate())
            )
         }

         delayMin?.let { DelayBadge(it) }

         Text(
            fontSize = 14.sp,
            text = it.direction
         )
      }

      if (delayMin != null) {
         Icon(painterResource(R.drawable.ic_gps), stringResource(R.string.live_arrival))
      }
   }
}

@Composable
private fun DelayBadge(delayMin: Int) {
   val delayText = if (delayMin == 0) {
      stringResource(R.string.on_time)
   } else if (delayMin >= 0) {
      stringResource(R.string.late, delayMin)
   } else {
      stringResource(R.string.early, -delayMin)
   }

   Text(
      fontSize = 14.sp,
      text = delayText,
      color = MaterialTheme.colorScheme.onTertiary,
      modifier = Modifier
         .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
         .background(MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.small)
         .padding(4.dp)
   )
}

private fun LazyListScope.bottomLoading(
   stopSchedule: ScheduleUiState,
   data: Outcome<ScheduleUiState>,
) {
   if (stopSchedule.hasAnyDataLeft == true) {
      item {
         Box(
            Modifier
               .fillMaxWidth()
               .height(32.dp),
            Alignment.Center
         ) {
            if (data is Outcome.Progress && data.style == LoadingStyle.ADDITIONAL_DATA) {
               CircularProgressIndicator(Modifier.size(32.dp))
            }
         }
      }
   }
}

@Composable
internal fun LineLabel(line: Line) {
   val lineColor = Color(line.color)
   val textColor: Color = if (lineColor.luminance() > LUMINANCE_HALF_BRIGHT) {
      Color.Black
   } else {
      Color.White
   }

   Box(Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.Center) {
      val shape = RoundedCornerShape(8.dp)
      Text(
         modifier = Modifier
            .background(lineColor, shape = shape)
            .border(Dp.Hairline, textColor, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
         text = line.label,
         textAlign = TextAlign.Center,
         fontWeight = FontWeight.ExtraBold,
         fontSize = 20.sp,
         color = textColor
      )
   }
}

@Composable
private fun Arrival.timeText(today: LocalDate, plusMinutes: Int = 0): String {
   val dateFormatter = LocalDateFormatter.current

   val timeText = dateFormatter.ofLocalizedTime().format(arrival.plusMinutes(plusMinutes.toLong()))
   val arrivalDate = arrival.toLocalDate()

   val dateText = when {
      arrivalDate == today -> ""
      arrivalDate == today.plusDays(1) -> stringResource(R.string.tomorrow)
      arrivalDate < today.plusDays(DAYS_IN_A_WEEK_MINUS_ONE) -> {
         arrivalDate.dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale.getDefault()
         )
      }

      else -> dateFormatter.ofLocalizedDate(FormatStyle.SHORT).format(arrivalDate)
   }

   return if (dateText.isEmpty()) {
      timeText
   } else {
      "$dateText, $timeText"
   }
}

private const val LUMINANCE_HALF_BRIGHT = 0.5
private const val DAYS_IN_A_WEEK_MINUS_ONE = 6L

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
      )
   }
}

internal val PREVIEW_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
internal val PREVIEW_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
internal val PREVIEW_EXPECTED_LINE_18 = Line(18, "18", 0xFF00000.toInt())

val PREVIEW_FAKE_LIST = ScheduleUiState(
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
