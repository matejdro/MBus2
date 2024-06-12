package com.matejdro.mbus.live

import app.cash.turbine.test
import com.matejdro.mbus.live.models.LiveArrivalsDto
import com.matejdro.mbus.schedule.FakeSchedulesService
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.test.flow.FakeUserPresenceProvider
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import si.inova.kotlinova.retrofit.InterceptionStyle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class LiveArrivalRepositoryImplTest {
   private val service = FakeSchedulesService()

   private val userPresenceProvider = FakeUserPresenceProvider().apply { isPresent = true }
   private val scope = TestScope(userPresenceProvider)

   var nowTime = LocalTime.of(10, 15)

   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { nowTime }
   )

   val lievArrivalRepository = LiveArrivalRepositoryImpl(service, timeProvider)

   @Test
   fun `Swap time and add delay for arrivals with live info`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 0),
                  4,
                  2
               ),
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 0),
                  -2,
                  6
               )
            )
         )
      )

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 4),
               "MB -> Mesto",
               4
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Update live info every minute`() = scope.runTest {
      service.provideArrivals(77, LiveArrivalsDto(emptyList()))

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 0),
                     4,
                     2
                  ),
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(11, 0),
                     -2,
                     6
                  )
               )
            )
         )

         delay(60_000)
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 4),
               "MB -> Mesto",
               4
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Do not update live info when user is not present`() = scope.runTest {
      service.provideArrivals(77, LiveArrivalsDto(emptyList()))

      userPresenceProvider.isPresent = false

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 0),
                     4,
                     2
                  ),
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(11, 0),
                     -2,
                     6
                  )
               )
            )
         )

         delay(60_000)
         runCurrent()
         expectNoEvents()
      }
   }

   @Test
   fun `Update live info immediately after user comes back`() = scope.runTest {
      service.provideArrivals(77, LiveArrivalsDto(emptyList()))

      userPresenceProvider.isPresent = false

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 0),
                     4,
                     2
                  ),
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(11, 0),
                     -2,
                     6
                  )
               )
            )
         )

         delay(60_000)
         runCurrent()

         userPresenceProvider.isPresent = true
         runCurrent()

         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 4),
               "MB -> Mesto",
               4
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Ignore live info when request takes too long`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.InfiniteLoad)

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         awaitItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Ignore live info when request reports network error`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.Error(NoNetworkException()))

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         awaitItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
         cancelAndConsumeRemainingEvents()
      }
   }

   @Test
   fun `Forward non-network errors`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.Error(IllegalStateException()))

      shouldThrow<IllegalStateException> {
         lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
            expectMostRecentItem()
         }
      }
   }

   @Test
   @Suppress("LongMethod") // Long test data
   fun `Emit regular data before loading`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 0),
                  4,
                  2
               ),
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 0),
                  -2,
                  6
               )
            )
         )
      )

      service.interceptAllFutureCallsWith(InterceptionStyle.InfiniteLoad)

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            )
         )

         service.completeInfiniteLoad()
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 4),
               "MB -> Mesto",
               4
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Ignore arrivals with no delay info`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 20),
                  null,
                  2
               ),
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 0),
                  -2,
                  6
               )
            )
         )
      )

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Clear out arrivals of the same line that came before the next live info`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 20),
                  4,
                  2
               ),
            )
         )
      )

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 24),
               "MB -> Mesto",
               4
            ),
         )
      }
   }

   @Test
   fun `Cut off lines arrivals without GPS info after 10 minutes`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 0),
                  -2,
                  6
               )
            )
         )
      )

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   fun `Keep very late arrivals on the board indefinitely`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 0),
                  50,
                  2
               ),
            )
         )
      )

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 50),
               "MB -> Mesto",
               liveDelayMin = 50
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
         )
      }
   }

   @Test
   @Suppress("LongMethod") // Fake test data are long
   fun `Show next day arrivals with earlier times`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 0),
                  4,
                  2
               ),
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(11, 0),
                  -2,
                  6
               )
            )
         )
      )

      val arrivals = listOf(
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 30, 10, 0),
            "MB -> Mesto"
         ),
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 30, 10, 20),
            "Mesto -> MB"
         ),
         Arrival(
            FAKE_LINE_6,
            LocalDateTime.of(2024, 3, 30, 11, 0),
            "MB -> Mesto"
         ),
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 30, 11, 20),
            "MB -> Mesto"
         ),
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 31, 8, 0),
            "MB -> Mesto"
         ),
      )

      lievArrivalRepository.addLiveArrivals(77, arrivals).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 4),
               "MB -> Mesto",
               4
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 20),
               "Mesto -> MB",
            ),
            Arrival(
               FAKE_LINE_6,
               LocalDateTime.of(2024, 3, 30, 10, 58),
               "MB -> Mesto",
               -2
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 11, 20),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 31, 8, 0),
               "MB -> Mesto",
            ),
         )
      }
   }

   @Test
   fun `Show tomorrows live arrivals`() = scope.runTest {
      nowTime = LocalTime.of(23, 50)

      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(0, 5),
                  10,
                  2
               ),
            )
         )
      )

      val arrivals = listOf(
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 31, 0, 5),
            "MB -> Mesto"
         ),
         Arrival(
            FAKE_LINE_2,
            LocalDateTime.of(2024, 3, 30, 0, 10),
            "MB -> Mesto"
         ),
      )

      lievArrivalRepository.addLiveArrivals(77, arrivals).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 31, 0, 15),
               "MB -> Mesto",
               10
            ),
         )
      }
   }
}

private val FAKE_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val FAKE_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())

private val FAKE_ARRIVALS = listOf(
   Arrival(
      FAKE_LINE_2,
      LocalDateTime.of(2024, 3, 30, 10, 0),
      "MB -> Mesto"
   ),
   Arrival(
      FAKE_LINE_2,
      LocalDateTime.of(2024, 3, 30, 10, 20),
      "Mesto -> MB"
   ),
   Arrival(
      FAKE_LINE_6,
      LocalDateTime.of(2024, 3, 30, 11, 0),
      "MB -> Mesto"
   ),
   Arrival(
      FAKE_LINE_2,
      LocalDateTime.of(2024, 3, 30, 11, 20),
      "MB -> Mesto"
   ),
)
