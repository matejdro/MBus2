package com.matejdro.mbus.favorites.ui

import app.cash.turbine.test
import com.matejdro.mbus.favorites.FakeFavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.LineStop
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.navigation.keys.FavoriteScheduleScreenKey
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.schedule.model.StopSchedule
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class FavoriteScheduleViewModelTest {
   private val scope = TestScope()
   private val repo = FakeFavoritesRepository()
   private val timeProvider = scope.virtualTimeProvider(
      currentLocalDate = { LocalDate.of(2024, 3, 30) },
      currentLocalTime = { LocalTime.of(9, 30) }
   )

   private val vm = FavoriteScheduleViewModel(scope.testCoroutineResourceManager(), repo, timeProvider, {})

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
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
      )

      val expectedData = FavoriteScheduleUiState(
         TEST_FAVORITE_1,
         arrivals.arrivals,
         false,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         listOf(TEST_STOP_7, TEST_STOP_8),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      repo.setFavorites(listOf(TEST_FAVORITE_1))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         arrivals.arrivals,
      )

      vm.key = FavoriteScheduleScreenKey(1)

      vm.onServiceRegistered()
      runCurrent()

      vm.schedule.value shouldBeSuccessWithData expectedData
   }

   @Test
   @Suppress("LongMethod") // Lots of data to define
   fun `Provide Paginated data`() = scope.runTest {
      val expectedFirstPage = FavoriteScheduleUiState(
         TEST_FAVORITE_1,
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 20),
               "Mesto -> MB"
            ),
         ),
         true,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         listOf(TEST_STOP_7, TEST_STOP_8),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      val expectedSecondPage = FavoriteScheduleUiState(
         TEST_FAVORITE_1,
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 10, 20),
               "Mesto -> MB"
            ),
            Arrival(
               TEST_EXPECTED_LINE_6,
               LocalDateTime.of(2024, 5, 30, 11, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 5, 30, 11, 20),
               "MB -> Mesto"
            ),
         ),
         false,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         listOf(TEST_STOP_7, TEST_STOP_8),
         emptySet(),
         ZonedDateTime.of(2024, 3, 30, 9, 30, 0, 0, ZoneId.of("UTC")),
         false
      )

      val firstPage = listOf(
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 10, 0),
            "MB -> Mesto"
         ),
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 10, 20),
            "Mesto -> MB"
         ),
      )

      val secondPage = listOf(
         Arrival(
            TEST_EXPECTED_LINE_6,
            LocalDateTime.of(2024, 5, 30, 11, 0),
            "MB -> Mesto"
         ),
         Arrival(
            TEST_EXPECTED_LINE_2,
            LocalDateTime.of(2024, 5, 30, 11, 20),
            "MB -> Mesto"
         ),
      )

      repo.setFavorites(listOf(TEST_FAVORITE_1))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         firstPage,
         secondPage
      )

      vm.key = FavoriteScheduleScreenKey(1)

      vm.onServiceRegistered()

      vm.schedule.test {
         runCurrent()
         expectMostRecentItem().shouldBeSuccessWithData(expectedFirstPage)

         vm.loadNextPage()
         runCurrent()
         expectMostRecentItem().shouldBeSuccessWithData(expectedSecondPage)
      }
   }

   @Test
   fun `Save line filter`() = scope.runTest {
      vm.key = FavoriteScheduleScreenKey(1)

      repo.setFavorites(listOf(TEST_FAVORITE_1))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         emptyList(),
      )

      vm.onServiceRegistered()
      runCurrent()

      vm.setFilter(setOf(LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)))
      runCurrent()

      repo.whitelistedLines shouldBe setOf(LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8))
   }

   @Test
   fun `Load data from different date`() = scope.runTest {
      val arrivals = StopSchedule(
         emptyList(),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
      )

      val expectedData = FavoriteScheduleUiState(
         TEST_FAVORITE_1,
         arrivals.arrivals,
         false,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         listOf(TEST_STOP_7, TEST_STOP_8),
         emptySet(),
         ZonedDateTime.of(2024, 3, 20, 8, 25, 0, 0, ZoneId.of("UTC")),
         true
      )

      repo.setFavorites(listOf(TEST_FAVORITE_1))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         arrivals.arrivals,
      )

      vm.key = FavoriteScheduleScreenKey(1)

      vm.onServiceRegistered()
      runCurrent()

      vm.changeDate(LocalDateTime.of(2024, 3, 20, 8, 25))
      runCurrent()

      repo.lastRequestedDate shouldBe LocalDateTime.of(2024, 3, 20, 8, 25)
      vm.schedule.value shouldBeSuccessWithData expectedData
   }

   @Test
   fun `Update favorite`() = scope.runTest {
      val arrivals = StopSchedule(
         emptyList(),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
      )

      repo.setFavorites(listOf(TEST_FAVORITE_1))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         arrivals.arrivals,
      )

      vm.key = FavoriteScheduleScreenKey(TEST_FAVORITE_1.id)

      vm.onServiceRegistered()
      runCurrent()

      vm.updateFavorite("Favorite C", listOf(TEST_STOP_7))
      runCurrent()

      repo.getScheduleForFavorite(TEST_FAVORITE_1.id, timeProvider.currentLocalDateTime()).data.test {
         runCurrent()
         assertSoftly(expectMostRecentItem().data.shouldNotBeNull()) {
            favorite.name shouldBe "Favorite C"

            allLines shouldBe listOf(
               LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
               LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
            )
         }
      }
   }

   @Test
   fun `Delete favorite`() = scope.runTest {
      val arrivals = StopSchedule(
         emptyList(),
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         false,
         listOf(TEST_EXPECTED_LINE_2, TEST_EXPECTED_LINE_6),
      )

      repo.setFavorites(listOf(TEST_FAVORITE_1, TEST_FAVORITE_2))
      repo.provideSchedule(
         TEST_FAVORITE_1,
         listOf(
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_7),
            LineStop(TEST_EXPECTED_LINE_2, TEST_STOP_8),
            LineStop(TEST_EXPECTED_LINE_6, TEST_STOP_8)
         ),
         arrivals.arrivals,
      )

      vm.key = FavoriteScheduleScreenKey(1)

      vm.onServiceRegistered()
      runCurrent()

      vm.deleteFavorite()
      runCurrent()

      repo.getListOfFavorites().test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(TEST_FAVORITE_2)
      }

      vm.schedule.value.data?.closeScreenAfterDeletion shouldBe true
   }
}

private val TEST_FAVORITE_1 = Favorite(
   1,
   "Favorite A",
   listOf(7, 8)
)

private val TEST_FAVORITE_2 = Favorite(
   2,
   "Favorite B",
   listOf(7, 8)
)

private val TEST_STOP_7 = StopInfo(
   7,
   "Forest 77",
   "A stop in the forest",
   "http://stopimage.com",
)

private val TEST_STOP_8 = StopInfo(
   8,
   "Forest 88",
   "A second stop in the forest",
)

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
