package com.matejdro.mbus.stops

import androidx.datastore.preferences.core.emptyPreferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.common.test.datastore.InMemoryDataStore
import com.matejdro.mbus.stops.di.StopsModule
import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.stops.model.StopDto
import com.matejdro.mbus.stops.model.Stops
import com.matejdro.mbus.stops.sqldelight.generated.Database
import com.matejdro.mbus.stops.sqldelight.generated.DbStopQueries
import io.kotest.matchers.shouldBe
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

class StopsRepositoryImplTest {
   private val testScope = TestScopeWithDispatcherProvider()
   private val service = FakeStopsService()

   @Test
   fun `Provide stops from network on first load`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         ),
         Stop(
            12,
            "Stop B",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }
   }

   @Test
   fun `Filter stops without coordinates`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               null
            ),
            StopDto(
               13,
               "Stop C",
               null,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }
   }

   @Test
   fun `Do not re-load stops after they have been loaded once`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         ),
         Stop(
            12,
            "Stop B",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }

      service.numLoads shouldBe 1
   }

   private val repo = StopsRepositoryImpl(
      service,
      createTestStopQueries(),
      InMemoryDataStore(emptyPreferences()),
      testScope.virtualTimeProvider()
   )

   @Test
   fun `Re-load stops after 48 hours`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         ),
         Stop(
            12,
            "Stop B",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }

      service.numLoads shouldBe 2
   }

   @Test
   fun `Show loading with cached data while new data loads`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         ),
         Stop(
            12,
            "Stop B",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem() shouldBeProgressWithData expectedStops
      }
   }

   @Test
   fun `Update new data after it loads`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )
         )
      )

      val expectedStops = listOf(
         Stop(
            13,
            "Stop C",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.InfiniteLoad)

      repo.getAllStops().test {
         runCurrent()

         service.providedStops = Stops(
            listOf(
               StopDto(
                  13,
                  "Stop C",
                  2.0,
                  2.0
               ),
            )
         )
         service.completeInfiniteLoad()
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }
   }

   @Test
   fun `Report error with cached data if data loading fails`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               10,
               "Stop A",
               1.0,
               1.0
            ),
            StopDto(
               12,
               "Stop B",
               2.0,
               2.0
            )

         )
      )

      val expectedStops = listOf(
         Stop(
            10,
            "Stop A",
            1.0,
            1.0
         ),
         Stop(
            12,
            "Stop B",
            2.0,
            2.0
         )
      )

      repo.getAllStops().test {
         runCurrent()
         cancelAndIgnoreRemainingEvents()
      }

      advanceTimeBy(50.hours)

      service.interceptNextCallWith(InterceptionStyle.Error(NoNetworkException()))

      repo.getAllStops().test {
         runCurrent()
         expectMostRecentItem().shouldBeErrorWith(
            expectedData = expectedStops,
            exceptionType = NoNetworkException::class.java
         )
      }
   }
}

private fun createTestStopQueries(): DbStopQueries {
   val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
   Database.Schema.create(driver)

   return StopsModule.provideStopQueries(driver)
}
