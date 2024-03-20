package com.matejdro.mbus.stops

import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.stops.model.StopDto
import com.matejdro.mbus.stops.model.Stops
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class StopsRepositoryImplTest {
   private val service = FakeService()

   private val repo = StopsRepositoryImpl(service)

   @Test
   fun `Provide stops`() = runTest {
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

      repo.getAllStops() shouldBe expectedStops
   }

   @Test
   fun `Filter stops without coordinates`() = runTest {
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

      repo.getAllStops() shouldBe expectedStops
   }
}
