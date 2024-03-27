package com.matejdro.mbus.schedule

import app.cash.turbine.test
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.schedule.models.LinesDto
import com.matejdro.mbus.schedule.models.StopScheduleDto
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val service = FakeSchedulesService()
   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { LocalTime.of(9, 30) }
   )

   private val repo = ScheduleRepositoryImpl(service, timeProvider)

   @BeforeEach
   fun setUp() {
      service.providedLines = PROVIDED_LINES
      service.provideSchedule(42, LocalDate.of(2024, 3, 30), PROVIDED_DATA_STOP_42)
   }

   @Test
   fun `Return mapped data from the server`() = scope.runTest {
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         expectMostRecentItem().items.data shouldBe StopSchedule(
            listOf(
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 10, 0),
                  "MB -> Mesto"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 10, 20),
                  "Mesto -> MB"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_6,
                  LocalDateTime.of(2024, 3, 30, 11, 0),
                  "MB -> Mesto"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 11, 20),
                  "MB -> Mesto"
               ),
            ),
            "Forest 77",
            "http://stopimage.com",
            "A stop in the forest",
         )
      }
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())

private val PROVIDED_DATA_STOP_42 = StopScheduleDto(
   listOf(
      StopScheduleDto.Schedule(
         2,
         "2",
         listOf(
            StopScheduleDto.Schedule.RouteAndSchedule(
               listOf(
                  LocalTime.of(10, 0),
                  LocalTime.of(11, 20),
               ),
               "MB -> Mesto"
            ),
            StopScheduleDto.Schedule.RouteAndSchedule(
               listOf(
                  LocalTime.of(10, 20),
               ),
               "Mesto -> MB"
            )
         )
      ),
      StopScheduleDto.Schedule(
         6,
         "6",
         listOf(
            StopScheduleDto.Schedule.RouteAndSchedule(
               listOf(
                  LocalTime.of(8, 0),
                  LocalTime.of(11, 0),
               ),
               "MB -> Mesto"
            )
         ),
      )
   ),
   StopScheduleDto.StaticData(
      "A stop in the forest",
      "http://stopimage.com",
      "Forest 77"
   )
)

private val PROVIDED_LINES = LinesDto(
   listOf(
      LinesDto.Line("6", 0xFF00FF00.toInt(), "Linija 6", 6),
      LinesDto.Line("2", 0xFFFF0000.toInt(), "Linija 2", 2),
   )
)
