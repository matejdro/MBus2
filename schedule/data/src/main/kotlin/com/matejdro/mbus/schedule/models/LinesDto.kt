package com.matejdro.mbus.schedule.models

import com.matejdro.mbus.network.converters.HexColor
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinesDto(
   @Json(name = "Lines")
   val lines: List<Line>,
) {
   @JsonClass(generateAdapter = true)
   data class Line(
      @Json(name = "Code")
      val code: String,
      @Json(name = "Color")
      @HexColor
      val color: Int,
      @Json(name = "Description")
      val description: String,
      @Json(name = "LineId")
      val lineId: Int,
   )
}
