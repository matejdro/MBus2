package com.matejdro.mbus.lines

import androidx.datastore.preferences.core.emptyPreferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.common.test.datastore.InMemoryDataStore
import com.matejdro.mbus.schedule.FakeSchedulesService
import com.matejdro.mbus.schedule.di.SchedulesModule
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.models.LinesDto
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbLineQueries
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import si.inova.kotlinova.retrofit.InterceptionStyle
import kotlin.time.Duration.Companion.hours

class LinesRepositoryImplTest {
   private val testScope = TestScopeWithDispatcherProvider()
   private val service = FakeSchedulesService()

   private val repo = LinesRepositoryImpl(
      service,
      createTestLineQueries(),
      InMemoryDataStore(emptyPreferences()),
      testScope.virtualTimeProvider()
   )

   @Test
   fun `Provide lines from network on first load`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(6, "6", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedLines
      }
   }

   @Test
   fun `Do not re-load lines after they have been loaded once`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(6, "6", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      repo.getAllLines().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedLines
      }

      service.numLineLoads shouldBe 1
   }

   @Test
   fun `Re-load lines after 48 hours`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(6, "6", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      repo.getAllLines().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedLines
      }

      service.numLineLoads shouldBe 2
   }

   @Test
   fun `Show loading with cached data while new data loads`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(6, "6", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)

      repo.getAllLines().test {
         runCurrent()
         expectMostRecentItem() shouldBeProgressWithData expectedLines
      }
   }

   @Test
   fun `Update new data after it loads`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(3, "3", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)

      repo.getAllLines().test {
         runCurrent()

         service.providedLines = LinesDto(
            listOf(
               LinesDto.Line("2", 0xFFFF0000.toInt(), "Linija 2", 2),
               LinesDto.Line("3", 0xFF00FF00.toInt(), "Linija 3", 3),
            )
         )

         service.completeInfiniteLoad()
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedLines
      }
   }

   @Test
   fun `Report error with cached data if data loading fails`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val expectedLines = listOf(
         Line(2, "2", 0xFFFF0000.toInt()),
         Line(6, "6", 0xFF00FF00.toInt()),
      )

      repo.getAllLines().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.Error(NoNetworkException()))

      repo.getAllLines().test {
         runCurrent()
         expectMostRecentItem().shouldBeErrorWith(
            expectedData = expectedLines,
            exceptionType = NoNetworkException::class.java
         )
      }
   }

   @Test
   fun `Only load from the network once at a time`() = testScope.runTest {
      service.providedLines = GENERAL_PROVIDED_LINES

      val latch = CompletableDeferred<Unit>()

      launch {
         repo.getAllLines().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      launch {
         repo.getAllLines().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      launch {
         repo.getAllLines().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      runCurrent()
      latch.complete(Unit)
      service.numLineLoads shouldBe 1
   }
}

private val GENERAL_PROVIDED_LINES = LinesDto(
   listOf(
      LinesDto.Line("2", 0xFFFF0000.toInt(), "Linija 2", 2),
      LinesDto.Line("6", 0xFF00FF00.toInt(), "Linija 6", 6),
   )
)

private fun createTestLineQueries(): DbLineQueries {
   val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
   Database.Schema.create(driver)

   return SchedulesModule.provideLineQueries(driver)
}
