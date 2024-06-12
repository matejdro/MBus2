package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.schedule.model.Line

data class LineStop(
   val line: Line,
   val stop: StopInfo,
)
