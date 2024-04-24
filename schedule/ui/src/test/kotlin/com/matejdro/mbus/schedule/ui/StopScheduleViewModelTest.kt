package com.matejdro.mbus.schedule.ui

import app.cash.turbine.test
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.FakeScheduleRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.stops.FakeStopsRepository
import com.matejdro.mbus.stops.model.Stop
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class StopScheduleViewModelTest {
   private val scope = TestScope()
   private val repo = FakeScheduleRepository()
   private val stopRepo = FakeStopsRepository()
   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { LocalTime.of(9, 30) }
   )

   private val vm = StopScheduleViewModel(scope.testCoroutineResourceManager(), repo, stopRepo, timeProvider)

   @Test
   fun `Provide data`() = scope.runTest {
      val arrivals = StopSchedule(
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
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
      )

      val expectedData = ScheduleUiState(
         arrivals.arrivals,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      repo.provideSchedule(
         12,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         arrivals.arrivals,
      )

      vm.key = StopScheduleScreenKey(12)

      vm.onServiceRegistered()
      runCurrent()

      vm.schedule.value shouldBeSuccessWithData expectedData
   }

   @Test
   @Suppress("LongMethod") // Lots of data to define
   fun `Provide Paginated data`() = scope.runTest {
      val expectedFirstPage = ScheduleUiState(
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 20),
               "Mesto -> MB"
            ),
         ),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         true,
         listOf(TEST_EXPECTED_LINE_2),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      val expectedSecondPage = ScheduleUiState(
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               TEST_EXPECTED_LINE_6,
               LocalDateTime.of(2024, 5, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 11, 20),
               "MB -> Mesto"
            ),
         ),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      val firstPage = listOf(
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 10, 0),
            "MB -> Mesto"
         ),
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 10, 20),
            "Mesto -> MB"
         ),
      )

      val secondPage = listOf(
         Arrival(
            TEST_EXPECTED_LINE_6,
            LocalDateTime.of(2024, 5, 30, 11, 0),
            "MB -> Mesto"
         ),
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 11, 20),
            "MB -> Mesto"
         ),
      )

      repo.provideSchedule(
         12,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         firstPage,
         secondPage
      )

      vm.key = StopScheduleScreenKey(12)

      vm.onServiceRegistered()

      vm.schedule.test {
         runCurrent()
         expectMostRecentItem().shouldBeSuccessWithData(expectedFirstPage)

         vm.loadNextPage()
         runCurrent()
         expectMostRecentItem().shouldBeSuccessWithData(expectedSecondPage)
      }
   }

   @Test
   fun `Save line filter`() = scope.runTest {
      vm.key = StopScheduleScreenKey(12)

      repo.provideSchedule(
         12,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         emptyList(),
      )

      stopRepo.provideStops(
         Outcome.Success(
            listOf(
               Stop(
                  12,
                  "Forest 77",
                  0.0,
                  0.0,
                  "http://stopimage.com",
                  "A stop in the forest",
               )
            )
         )
      )

      vm.onServiceRegistered()
      runCurrent()

      vm.setFilter(setOf(6))
      runCurrent()

      stopRepo.getStop(12).first() shouldBe Stop(
         12,
         "Forest 77",
         0.0,
         0.0,
         "http://stopimage.com",
         "A stop in the forest",
         whitelistedLines = setOf(6)
      )
   }

   @Test
   fun `Load data from different date`() = scope.runTest {
      val arrivals = StopSchedule(
         emptyList(),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         emptyList(),
      )

      val expectedData = ScheduleUiState(
         arrivals.arrivals,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         emptyList(),
         emptySet(),
         ZonedDateTime.of(2024, 3, 20, 8, 25, 0, 0, ZoneId.of("UTC")),
         true
      )

      repo.provideSchedule(
         12,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         arrivals.arrivals,
      )

      vm.key = StopScheduleScreenKey(12)

      vm.onServiceRegistered()
      runCurrent()

      vm.changeDate(LocalDateTime.of(2024, 3, 20, 8, 25))
      runCurrent()

      repo.lastRequestedDate shouldBe LocalDateTime.of(2024, 3, 20, 8, 25)
      vm.schedule.value shouldBeSuccessWithData expectedData
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
