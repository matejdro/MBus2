package com.matejdro.mbus.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.R
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import com.matejdro.mbus.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.FakeAndroidTimeProvider
import si.inova.kotlinova.core.time.TimeProvider
import si.inova.kotlinova.navigation.screens.Screen
import java.time.LocalDate
import java.time.LocalDateTime
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
         ScheduleScreenContent(
            state,
            timeProvider,
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
   data: Outcome<StopSchedule>,
   timeProvider: TimeProvider,
) {
   Column {
      TopAppBar(title = { data.data?.stopName?.let { Text(it) } })

      TopLoading(data)
      TopError(data)

      val stopSchedule = data.data

      if (stopSchedule != null) {
         LazyColumn(Modifier.weight(1f)) {
            stopImageItem(stopSchedule)

            itemsWithDivider(stopSchedule.arrivals) {
               ScheduleItem(it, timeProvider)
            }
         }
      }
   }
}

@Composable
private fun ColumnScope.TopError(data: Outcome<StopSchedule>) {
   if (data is Outcome.Error) {
      Text(
         text = data.exception.commonUserFriendlyMessage(),
         Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .padding(32.dp),
         color = MaterialTheme.colorScheme.error
      )
   }
}

@Composable
private fun ColumnScope.TopLoading(data: Outcome<StopSchedule>) {
   if (data is Outcome.Progress) {
      CircularProgressIndicator(
         Modifier.Companion
            .align(Alignment.CenterHorizontally)
            .padding(32.dp)
      )
   }
}

private fun LazyListScope.stopImageItem(stopSchedule: StopSchedule) {
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
   Row(
      Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
   ) {
      LineLabel(it.line)

      Column {
         Text(
            fontSize = 20.sp,
            text = it.timeText(timeProvider.currentLocalDate())
         )

         Text(
            fontSize = 14.sp,
            text = it.direction
         )
      }
   }
}

@Composable
private fun LineLabel(line: Line) {
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
private fun Arrival.timeText(today: LocalDate): String {
   val dateFormatter = LocalDateFormatter.current

   val timeText = dateFormatter.ofLocalizedTime().format(arrival)
   val arrivalDate = arrival.toLocalDate()

   val dateText = when {
      arrivalDate == today -> ""
      arrivalDate == today.plusDays(1) -> stringResource(R.string.tomorrow)
      arrivalDate < today.plusDays(DAYS_IN_A_WEEK_MINUS_ONE) -> {
         arrivalDate.dayOfWeek.getDisplayName(
            TextStyle.FULL_STANDALONE,
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
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenErrorPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Error(UnknownCauseException()),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun ScheduleScreenRefreshErrorPreview() {
   PreviewTheme() {
      ScheduleScreenContent(
         Outcome.Error(UnknownCauseException(), PREVIEW_FAKE_LIST),
         FakeAndroidTimeProvider(currentLocalDate = { LocalDate.of(2024, 3, 30) }),
      )
   }
}

private val PREVIEW_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val PREVIEW_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
private val PREVIEW_EXPECTED_LINE_18 = Line(18, "18", 0xFF00000.toInt())

val PREVIEW_FAKE_LIST = StopSchedule(
   listOf(
      Arrival(
         PREVIEW_EXPECTED_LINE_2,
         LocalDateTime.of(2024, 3, 30, 10, 0),
         "MB -> Mesto"
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_2,
         LocalDateTime.of(2024, 3, 31, 10, 20),
         "Mesto -> MB"
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_6,
         LocalDateTime.of(2024, 4, 2, 11, 0),
         "MB -> Mesto"
      ),
      Arrival(
         PREVIEW_EXPECTED_LINE_18,
         LocalDateTime.of(2024, 4, 20, 11, 20),
         "MB -> Mesto"
      ),
   ),
   "Forest 77",
   "http://stopimage.com",
   "A stop in the forest",
)
