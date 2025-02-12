package com.matejdro.mbus.network.converters

import com.matejdro.mbus.common.di.ApplicationScope
import com.matejdro.mbus.network.di.MoshiAdapter
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import javax.inject.Inject

/**
 * Conver color string to ARGB int
 */
@JsonQualifier
annotation class HexColor

@ContributesMultibinding(ApplicationScope::class)
class ColorAdapter @Inject constructor() : MoshiAdapter {
   @FromJson
   @HexColor
   @Suppress("MagicNumber")
   fun fromHexToInt(jsonValue: String): Int? {
      val colorHex = jsonValue.removePrefix("#").trim()
      return when (colorHex.length) {
         0 -> null
         8 -> colorHex.toInt(16)
         6 -> colorHex.toInt(16) or 0xFF000000.toInt() // Set alpha to 0xFF
         else -> error("Unknown color hex: $jsonValue")
      }
   }

   @ToJson
   fun fromIntToHex(
      @Suppress("UNUSED_PARAMETER")
      @HexColor
      value: Int?,
   ): String {
      throw UnsupportedOperationException("Color to JSON not supported for now")
   }
}
