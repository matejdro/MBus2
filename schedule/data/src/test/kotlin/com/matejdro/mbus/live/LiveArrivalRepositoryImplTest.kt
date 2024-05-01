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
import si.inova.kotlinova.retrofit.InterceptionStyle
import java.time.LocalDateTime
import java.time.LocalTime

class LiveArrivalRepositoryImplTest {
   private val service = FakeSchedulesService()

   val lievArrivalRepository = LiveArrivalRepositoryImpl(service)

   private val userPresenceProvider = FakeUserPresenceProvider().apply { isPresent = true }

   private val scope = TestScope(userPresenceProvider)

   private val defaultBefore = LocalDateTime.of(2024, 3, 30, 9, 0)

   @Test
   fun `Swap time and add delay for arrivals with live info`() = scope.runTest {
      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 20),
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 24),
               "Mesto -> MB",
               4
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 20),
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
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 24),
               "Mesto -> MB",
               4
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()
         expectMostRecentItem()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 20),
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
   fun `Update live info immediately after user comes`() = scope.runTest {
      service.provideArrivals(77, LiveArrivalsDto(emptyList()))

      userPresenceProvider.isPresent = false

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()
         expectMostRecentItem()

         service.provideArrivals(
            77,
            LiveArrivalsDto(
               listOf(
                  LiveArrivalsDto.LiveArrivalDto(
                     LocalTime.of(10, 20),
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
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 24),
               "Mesto -> MB",
               4
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         awaitItem() shouldBe listOf(
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
      }
   }

   @Test
   fun `Ignore live info when request reports network error`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.Error(NoNetworkException()))

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         awaitItem() shouldBe listOf(
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
         cancelAndConsumeRemainingEvents()
      }
   }

   @Test
   fun `Forward non-network errors`() = scope.runTest {
      service.interceptAllFutureCallsWith(InterceptionStyle.Error(IllegalStateException()))

      shouldThrow<IllegalStateException> {
         lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
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
                  LocalTime.of(10, 20),
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
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
            )
         )

         service.completeInfiniteLoad()
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 24),
               "Mesto -> MB",
               4
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, defaultBefore).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
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
   fun `Clear out arrivals without live info before specific time`() = scope.runTest {
      val clearBefore = LocalDateTime.of(2024, 3, 30, 10, 30)

      service.provideArrivals(
         77,
         LiveArrivalsDto(
            listOf(
               LiveArrivalsDto.LiveArrivalDto(
                  LocalTime.of(10, 20),
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

      lievArrivalRepository.addLiveArrivals(77, FAKE_ARRIVALS, clearBefore).test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Arrival(
               FAKE_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 24),
               "Mesto -> MB",
               4
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
