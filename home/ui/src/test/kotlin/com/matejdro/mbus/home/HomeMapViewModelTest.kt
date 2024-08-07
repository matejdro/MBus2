package com.matejdro.mbus.home

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.matejdro.mbus.stops.FakeStopsRepository
import com.matejdro.mbus.stops.model.Stop
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.android.Location
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.ThrowingErrorReporter
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import java.io.IOException

class HomeMapViewModelTest {
   private val testScope = TestScopeWithDispatcherProvider()
   private val stopsRepo = FakeStopsRepository()
   private var providedLocation: () -> Location = { throw IllegalStateException("Location not faked") }
   private val errorReporter = ThrowingErrorReporter(testScope).apply { reportToTestScope = false }

   val coroutineResourceManager = CoroutineResourceManager(testScope.backgroundScope, errorReporter)
   private val viewModel = HomeMapViewModel(coroutineResourceManager, stopsRepo, {}, { providedLocation() }, errorReporter)

   @Test
   fun `Provide stops when loading stops via map area`() = testScope.runTest {
      stopsRepo.provideStops(Outcome.Success(FAKE_STOPS))

      viewModel.loadStops(
         LatLngBounds(LatLng(0.0015, 0.00450), LatLng(0.0025, 0.0055))
      )
      runCurrent()

      viewModel.state.value.shouldBeSuccessWithData(
         HomeState(
            listOf(
               Stop(
                  5,
                  "Stop E",
                  0.002,
                  0.005
               )
            )
         )
      )
   }

   @Test
   fun `Do not re-load when rectangle moves slightly`() = testScope.runTest {
      stopsRepo.provideStops(Outcome.Success(FAKE_STOPS))

      viewModel.loadStops(
         LatLngBounds(LatLng(1.5, 15.0), LatLng(1.501, 15.01))
      )
      runCurrent()
      viewModel.loadStops(
         LatLngBounds(LatLng(1.50003, 15.0003), LatLng(1.50103, 15.0103))
      )
      runCurrent()

      stopsRepo.numLoads shouldBe 1
   }

   @Test
   fun `Do not load at all  when user is too zoomed in rectangle moves slightly`() = testScope.runTest {
      stopsRepo.provideStops(Outcome.Success(FAKE_STOPS))

      viewModel.loadStops(
         LatLngBounds(LatLng(1.5, 15.0), LatLng(1.6, 15.1))
      )
      runCurrent()

      stopsRepo.numLoads shouldBe 0
   }

   @Test
   fun `Re-load when rectangle moves singificantly`() = testScope.runTest {
      stopsRepo.provideStops(Outcome.Success(FAKE_STOPS))

      viewModel.loadStops(
         LatLngBounds(LatLng(1.5, 15.0), LatLng(1.501, 15.01))
      )
      runCurrent()
      viewModel.loadStops(
         LatLngBounds(LatLng(2.5, 16.0), LatLng(2.501, 16.01))
      )
      runCurrent()

      stopsRepo.numLoads shouldBe 2
   }

   @Test
   fun `Provide location when requesting map move`() = testScope.runTest {
      providedLocation = { Location(1.0, 2.0) }

      viewModel.moveMapToUser()
      runCurrent()

      viewModel.state.value.data.shouldNotBeNull().event shouldBe HomeEvent.MoveMap(LatLng(1.0, 2.0))
   }

   @Test
   fun `Reset event when requested`() = testScope.runTest {
      providedLocation = { Location(1.0, 2.0) }

      viewModel.moveMapToUser()
      runCurrent()

      viewModel.notifyEventHandled()
      runCurrent()

      viewModel.state.value.data.shouldNotBeNull().event.shouldBeNull()
   }

   @Test
   fun `Report location retrieving errors`() = testScope.runTest {
      val exception = IOException("No location")

      providedLocation = { throw exception }

      viewModel.moveMapToUser()
      runCurrent()

      errorReporter.receivedExceptions.shouldContainExactly(exception)
   }
}

private val FAKE_STOPS = listOf(
   Stop(
      1,
      "Stop A",
      0.001,
      0.005
   ),
   Stop(
      2,
      "Stop B",
      0.002,
      0.004
   ),
   Stop(
      3,
      "Stop C",
      0.003,
      0.005
   ),
   Stop(
      4,
      "Stop D",
      0.002,
      0.006
   ),
   Stop(
      5,
      "Stop E",
      0.002,
      0.005
   ),
)
