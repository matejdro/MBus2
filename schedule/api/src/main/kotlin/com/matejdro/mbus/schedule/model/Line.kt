package com.matejdro.mbus.schedule.model

import si.inova.kotlinova.core.data.Immutable

@Immutable
data class Line(
   val id: Int,
   val label: String,
   val color: Int?,
)
