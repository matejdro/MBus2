package com.matejdro.mbus.schedule.models

import com.matejdro.mbus.network.converters.HexColor
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.sqldelight.generated.DbLine
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
      val color: Int?,
      @Json(name = "Description")
      val description: String,
      @Json(name = "LineId")
      val lineId: Int,
   )
}

fun LinesDto.Line.toDbLine(): DbLine {
   return DbLine(
      lineId.toLong(),
      code,
      color?.toLong()
   )
}

fun DbLine.toLine(): Line {
   return Line(
      id.toInt(),
      label,
      color?.toInt()
   )
}
