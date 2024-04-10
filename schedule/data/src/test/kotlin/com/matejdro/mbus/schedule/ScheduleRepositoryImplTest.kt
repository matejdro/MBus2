package com.matejdro.mbus.schedule

import androidx.datastore.preferences.core.emptyPreferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.common.test.datastore.InMemoryDataStore
import com.matejdro.mbus.lines.LinesRepositoryImpl
import com.matejdro.mbus.schedule.di.SchedulesModule
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import com.matejdro.mbus.schedule.models.LinesDto
import com.matejdro.mbus.schedule.models.StopScheduleDto
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.stops.FakeStopsRepository
import com.matejdro.mbus.stops.model.Stop
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.LoadingStyle
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import si.inova.kotlinova.retrofit.InterceptionStyle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.Duration.Companion.hours

class ScheduleRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val db = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
      Database.Schema.create(it)
   }

   private val service = FakeSchedulesService()
   private val stopsRepository = FakeStopsRepository()
   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { LocalTime.of(9, 30) }
   )
   private val linesRepo = LinesRepositoryImpl(
      service,
      SchedulesModule.provideLineQueries(db),
      InMemoryDataStore(emptyPreferences()),
      timeProvider
   )

   private val repo = ScheduleRepositoryImpl(
      service,
      timeProvider,
      stopsRepository,
      SchedulesModule.provideArrivalQueries(db)
   )

   @BeforeEach
   fun setUp() {
      service.providedLines = PROVIDED_LINES
      service.provideSchedule(42, LocalDate.of(2024, 3, 30), PROVIDED_DATA_STOP_42_MAR_30)
      service.provideSchedule(42, LocalDate.of(2024, 3, 31), PROVIDED_DATA_STOP_42_MAR_31)
      stopsRepository.provideStops(
         Outcome.Success(
            listOf(
               Stop(42, "Forest 77", 1.0, 1.0)
            )
         )
      )
   }

   @Test
   fun `Return mapped data from the server`() = scope.runTest {
      prepareLines()
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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }
   }

   @Test
   fun `Return mapped data from the server for the subsequent days`() = scope.runTest {
      prepareLines()
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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }
   }

   @Test
   fun `Return regular loading outcome while loading`() = scope.runTest {
      prepareLines()
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
      prepareLines()
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

   @Test
   fun `Return data from cache after reload`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }

      service.numScheduleLoads shouldBe 1
   }

   @Test
   fun `Return data from server again when cache is expired`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      delay(50.hours)

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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }

      service.numScheduleLoads shouldBe 2
   }

   @Test
   fun `Ignore cache duration of other stops`() = scope.runTest {
      prepareLines()
      service.provideSchedule(43, LocalDate.of(2024, 3, 30), PROVIDED_DATA_STOP_42_MAR_30)

      val streamForStop42 = repo.getScheduleForStop(42)
      val streamForStop43 = repo.getScheduleForStop(42)

      streamForStop42.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      delay(30.hours)

      streamForStop43.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      delay(30.hours)

      streamForStop43.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      service.numScheduleLoads shouldBe 2
   }

   @Test
   fun `Return updated data from server again when cache is expired`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      service.provideSchedule(42, LocalDate.of(2024, 3, 30), PROVIDED_DATA_STOP_42_MAR_30_MODIFIED)
      delay(50.hours)

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
                  LocalDateTime.of(2024, 3, 30, 10, 25),
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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }

      service.numScheduleLoads shouldBe 2
   }

   @Test
   fun `Return loading with old data even if data has expired`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      delay(50.hours)
      service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)

      stream.data.test {
         runCurrent()
         expectMostRecentItem() shouldBeProgressWithData StopSchedule(
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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }
   }

   @Test
   @Suppress("LongMethod") // Long test data
   fun `Load subsequent days from DB when available`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         stream.nextPage()
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

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
            true,
            TEST_EXPECTED_ALL_LINES,
         )

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
            true,
            TEST_EXPECTED_ALL_LINES,
         )
      }

      service.numScheduleLoads shouldBe 2
   }

   @Test
   @Suppress("LongMethod") // Long test data
   fun `Continue loading cached pages despite network issues`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         stream.nextPage()
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      delay(50.hours)
      service.interceptAllFutureCallsWith(InterceptionStyle.Error(NoNetworkException()))

      stream.data.test {
         runCurrent()
         expectMostRecentItem().shouldBeErrorWith(
            expectedData = StopSchedule(
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
               true,
               TEST_EXPECTED_ALL_LINES
            ),
            exceptionType = NoNetworkException::class.java
         )

         stream.nextPage()
         runCurrent()
         expectMostRecentItem().shouldBeErrorWith(
            expectedData = StopSchedule(
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
               true,
               TEST_EXPECTED_ALL_LINES,
            ),
            exceptionType = NoNetworkException::class.java
         )
      }
   }

   @Test
   fun `Provide line whitelist`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         stopsRepository.setWhitelistedLines(42, setOf(6, 2))
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
            true,
            TEST_EXPECTED_ALL_LINES,
            setOf(6, 2)
         )
      }
   }

   @Test
   fun `Filter lines when whitelist is set`() = scope.runTest {
      prepareLines()
      val stream = repo.getScheduleForStop(42)

      stream.data.test {
         runCurrent()

         stopsRepository.setWhitelistedLines(42, setOf(2))
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
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 11, 20),
                  "MB -> Mesto"
               ),
            ),
            "Forest 77",
            "http://stopimage.com",
            "A stop in the forest",
            true,
            TEST_EXPECTED_ALL_LINES,
            setOf(2)
         )
      }
   }

   private suspend fun TestScope.prepareLines() {
      linesRepo.getAllLines().test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
private val TEST_EXPECTED_ALL_LINES = listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6)

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

private val PROVIDED_DATA_STOP_42_MAR_30_MODIFIED = StopScheduleDto(
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
                  LocalTime.of(10, 25),
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

private val PROVIDED_LINES = LinesDto(
   listOf(
      LinesDto.Line("6", 0xFF00FF00.toInt(), "Linija 6", 6),
      LinesDto.Line("2", 0xFFFF0000.toInt(), "Linija 2", 2),
   )
)
