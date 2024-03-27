package com.matejdro.mbus.schedule.model

data class StopSchedule(
   val arrivals: List<Arrival>,
   val stopName: String,
   val stopImage: String?,
   val stopDescription: String,
)
