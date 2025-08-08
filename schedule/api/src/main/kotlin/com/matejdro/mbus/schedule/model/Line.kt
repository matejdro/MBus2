package com.matejdro.mbus.schedule.model

import androidx.compose.runtime.Immutable

@Immutable
data class Line(
   val id: Int,
   val label: String,
   val color: Int?,
)
