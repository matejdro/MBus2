package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.schedule.model.StopSchedule

interface ScheduleRepository {
   fun getScheduleForStop(stopId: Int): PaginatedDataStream<StopSchedule>
}
