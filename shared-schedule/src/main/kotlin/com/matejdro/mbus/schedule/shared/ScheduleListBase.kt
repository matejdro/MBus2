package com.matejdro.mbus.schedule.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.ui.lists.DetectScrolledToBottom
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.core.time.TimeProvider
import java.time.LocalDate

@Composable
fun StopList(
   arrivals: List<Arrival>,
   stopImage: String?,
   timeProvider: TimeProvider,
   hasAnyDataLeftToLoad: Boolean,
   loadingMore: Boolean,
   loadNextPage: () -> Unit,
   modifier: Modifier = Modifier,
) {
   val state = rememberLazyListState()

   state.DetectScrolledToBottom(loadNextPage)

   LazyColumn(modifier, state) {
      stopImage?.let { stopImageItem(it) }

      itemsWithDivider(arrivals) {
         ScheduleItem(it, timeProvider)
      }

      bottomLoading(hasAnyDataLeftToLoad, loadingMore)
   }
}

private fun LazyListScope.stopImageItem(imageUrl: String) {
   item {
      var showImage by remember { mutableStateOf(true) }
      if (showImage) {
         AsyncImage(
            model = imageUrl,
            modifier = Modifier
               .fillMaxWidth()
               .height(200.dp),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            onState = { if (it is AsyncImagePainter.State.Error) showImage = false }
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
   hasAnyDataLeftToLoad: Boolean,
   loadingMore: Boolean,
) {
   if (hasAnyDataLeftToLoad == true) {
      item {
         Box(
            Modifier
               .fillMaxWidth()
               .height(32.dp),
            Alignment.Center
         ) {
            if (loadingMore) {
               CircularProgressIndicator(Modifier.size(32.dp))
            }
         }
      }
   }
}

@Composable
fun LineLabel(line: Line, modifier: Modifier = Modifier) {
   val lineColor = Color(line.color)
   val textColor: Color = if (lineColor.luminance() > LUMINANCE_HALF_BRIGHT) {
      Color.Black
   } else {
      Color.White
   }

   Box(modifier.widthIn(min = 48.dp), contentAlignment = Alignment.Center) {
      val shape = RoundedCornerShape(8.dp)
      Text(
         modifier = Modifier
            .background(lineColor, shape = shape)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface, shape)
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
   val dateFormatter = si.inova.kotlinova.compose.time.LocalDateFormatter.current

   val timeText = dateFormatter.ofLocalizedTime().format(arrival.plusMinutes(plusMinutes.toLong()))
   val arrivalDate = arrival.toLocalDate()

   val dateText = when {
      arrivalDate == today -> ""
      arrivalDate == today.plusDays(1) -> stringResource(R.string.tomorrow)
      arrivalDate < today.plusDays(DAYS_IN_A_WEEK_MINUS_ONE) -> {
         arrivalDate.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale.getDefault()
         )
      }

      else -> dateFormatter.ofLocalizedDate(java.time.format.FormatStyle.SHORT).format(arrivalDate)
   }

   return if (dateText.isEmpty()) {
      timeText
   } else {
      "$dateText, $timeText"
   }
}

private const val LUMINANCE_HALF_BRIGHT = 0.5
private const val DAYS_IN_A_WEEK_MINUS_ONE = 6L
