package com.matejdro.mbus.home

import com.matejdro.mbus.stops.FakeStopsRepository
import com.matejdro.mbus.stops.model.Stop
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager

class HomeMapViewModelTest {
   private val testScope = TestScope()
   private val stopsRepo = FakeStopsRepository()

   private val viewModel = HomeMapViewModel(testScope.testCoroutineResourceManager(), stopsRepo)

   @Test
   fun `Provide stops at startup`() = testScope.runTest {
      stopsRepo.provideStops(Outcome.Success(FAKE_STOPS))

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.stops.value.shouldBeSuccessWithData(FAKE_STOPS)
   }
}

private val FAKE_STOPS = listOf(
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
