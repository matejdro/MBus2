package com.matejdro.mbus.schedule

import com.matejdro.mbus.common.data.PaginatedDataStream
import com.matejdro.mbus.schedule.model.StopSchedule
import java.time.LocalDateTime

interface ScheduleRepository {
   fun getScheduleForStop(
      stopId: Int,
      from: LocalDateTime,
      ignoreLineWhitelist: Boolean = false,
      includeLive: Boolean = true,
   ): PaginatedDataStream<StopSchedule>
}
