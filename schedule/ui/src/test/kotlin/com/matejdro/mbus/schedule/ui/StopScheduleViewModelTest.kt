package com.matejdro.mbus.schedule.ui

import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.schedule.FakeScheduleRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import java.time.LocalDateTime

class StopScheduleViewModelTest {
   private val scope = TestScope()
   private val repo = FakeScheduleRepository()

   private val vm = StopScheduleViewModel(scope.testCoroutineResourceManager(), repo)

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

      vm.schedule.value shouldBeSuccessWithData arrivals
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
