package com.matejdro.mbus.network.converters

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.di.MoshiAdapter
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@ContributesMultibinding(ApplicationScope::class)
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
