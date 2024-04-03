package com.matejdro.mbus.schedule

import app.cash.turbine.test
import com.matejdro.mbus.lines.FakeLinesRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.schedule.models.StopScheduleDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import si.inova.kotlinova.retrofit.InterceptionStyle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val service = FakeSchedulesService()
   private val linesRepo = FakeLinesRepository()
   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { LocalTime.of(9, 30) }
   )

   private val repo = ScheduleRepositoryImpl(service, timeProvider, linesRepo)

   @BeforeEach
   fun setUp() {
      linesRepo.provideLines(Outcome.Success(PROVIDED_LINES))
      service.provideSchedule(42, LocalDate.of(2024, 3, 30), PROVIDED_DATA_STOP_42_MAR_30)
      service.provideSchedule(42, LocalDate.of(2024, 3, 31), PROVIDED_DATA_STOP_42_MAR_31)
   }

   @Test
   fun `Return mapped data from the server`() = scope.runTest {
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData StopSchedule(
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
            true
         )
      }
   }

   @Test
   fun `Return mapped data from the server for the subsequent days`() = scope.runTest {
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         stream.nextPage()
         runCurrent()

         expectMostRecentItem() shouldBeSuccessWithData StopSchedule(
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
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 31, 5, 20),
                  "MB -> Mesto"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_6,
                  LocalDateTime.of(2024, 3, 31, 9, 0),
                  "MB -> Mesto"
               ),
            ),
            "Forest 77",
            "http://stopimage.com",
            "A stop in the forest",
            true
         )
      }
   }

   @Test
   @Disabled("Not working for now, until we add caching to display previous data")
   fun `Return regular loading outcome while loading`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.InfiniteLoad)

      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         expectMostRecentItem()
            .shouldBeInstanceOf<Outcome.Progress<*>>()
            .style
            .shouldBe(LoadingStyle.NORMAL)
      }
   }

   @Test
   fun `Return loading more outcome while loading extra pages`() = scope.runTest {
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         service.interceptAllFutureCallsWith(InterceptionStyle.InfiniteLoad)
         stream.nextPage()
         runCurrent()

         expectMostRecentItem()
            .shouldBeInstanceOf<Outcome.Progress<*>>()
            .style
            .shouldBe(LoadingStyle.ADDITIONAL_DATA)
      }
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())

private val PROVIDED_DATA_STOP_42_MAR_30 = StopScheduleDto(
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

private val PROVIDED_DATA_STOP_42_MAR_31 = StopScheduleDto(
   listOf(
      StopScheduleDto.Schedule(
         2,
         "2",
         listOf(
            StopScheduleDto.Schedule.RouteAndSchedule(
               listOf(
                  LocalTime.of(5, 20),
               ),
               "MB -> Mesto"
            ),
         )
      ),
      StopScheduleDto.Schedule(
         6,
         "6",
         listOf(
            StopScheduleDto.Schedule.RouteAndSchedule(
               listOf(
                  LocalTime.of(9, 0),
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

private val PROVIDED_LINES = listOf(
   Line(6, "6", 0xFF00FF00.toInt()),
   Line(2, "2", 0xFFFF0000.toInt()),
)
