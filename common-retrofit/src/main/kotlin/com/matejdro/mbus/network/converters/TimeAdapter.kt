package com.matejdro.mbus.network.converters

import com.matejdro.mbus.network.di.MoshiAdapter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ContributesIntoSet(AppScope::class)
class TimeAdapter @Inject constructor() : MoshiAdapter {
   @FromJson
   fun fromLocalTimeToInt(jsonValue: String): LocalTime {
      return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(jsonValue))
   }

   @ToJson
   fun fromIntToLocalTime(
      value: LocalTime,
   ): String {
      return DateTimeFormatter.ISO_LOCAL_TIME.format(value)
   }
}
