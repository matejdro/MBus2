package com.matejdro.mbus.schedule.models

import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.sqldelight.generated.SelectAllOnStop
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun SelectAllOnStop.toArrival(): Arrival {
   return Arrival(
      Line(
         lineId.toInt(),
         label,
         color?.toInt()
      ),
      LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(arrivalTime)),
      direction
   )
}
