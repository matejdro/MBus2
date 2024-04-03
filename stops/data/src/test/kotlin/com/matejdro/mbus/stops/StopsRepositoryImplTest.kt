package com.matejdro.mbus.stops

import androidx.datastore.preferences.core.emptyPreferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.common.test.datastore.InMemoryDataStore
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbStopQueries
import com.matejdro.mbus.stops.di.StopsModule
import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.stops.model.StopDto
import com.matejdro.mbus.stops.model.Stops
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
import java.time.Instant
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
            12,
            "Stop C",
            3.0,
            4.0
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
                  12,
                  "Stop C",
                  3.0,
                  4.0
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

   @Test
   fun `Only load from the network once at a time`() = testScope.runTest {
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

      val latch = CompletableDeferred<Unit>()

      launch {
         repo.getAllStops().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      launch {
         repo.getAllStops().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      launch {
         repo.getAllStops().test {
            latch.join()
            cancelAndIgnoreRemainingEvents()
         }
      }

      runCurrent()
      latch.complete(Unit)
      service.numLoads shouldBe 1
   }

   @Test
   fun `Filter stops based on coordinates`() = testScope.runTest {
      service.providedStops = Stops(
         listOf(
            StopDto(
               1,
               "Stop A",
               1.0,
               20.0
            ),
            StopDto(
               2,
               "Stop B",
               2.0,
               10.0
            ),
            StopDto(
               3,
               "Stop C",
               3.0,
               20.0
            ),
            StopDto(
               4,
               "Stop D",
               2.0,
               30.0
            ),
            StopDto(
               5,
               "Stop E",
               2.0,
               20.0
            ),
         )
      )

      val expectedStops = listOf(
         Stop(
            5,
            "Stop E",
            2.0,
            20.0
         ),
      )

      repo.getAllStopsWithinRect(
         minLat = 1.5,
         maxLat = 2.5,
         minLon = 15.0,
         maxLon = 25.0,
      ).test {
         runCurrent()
         expectMostRecentItem() shouldBeSuccessWithData expectedStops
      }
   }

   @Test
   fun `Remember last stop update`() = testScope.runTest {
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

      repo.getAllStops().test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      repo.setLastStopUpdate(10, Instant.ofEpochMilli(100L))
      repo.setLastStopUpdate(12, Instant.ofEpochMilli(200L))

      repo.getLastStopUpdate(10) shouldBe Instant.ofEpochMilli(100L)
   }

   @Test
   fun `Remember last stop update even after updates`() = testScope.runTest {
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

      repo.getAllStops().test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      repo.setLastStopUpdate(10, Instant.ofEpochMilli(100L))
      repo.setLastStopUpdate(12, Instant.ofEpochMilli(200L))

      advanceTimeBy(50.hours)

      repo.getAllStops().test {
         runCurrent()
         cancelAndConsumeRemainingEvents()
      }

      repo.getLastStopUpdate(10) shouldBe Instant.ofEpochMilli(100L)
   }
}

private fun createTestStopQueries(): DbStopQueries {
   val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
   Database.Schema.create(driver)

   return StopsModule.provideStopQueries(driver)
}
