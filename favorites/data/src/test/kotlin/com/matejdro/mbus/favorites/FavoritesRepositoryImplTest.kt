package com.matejdro.mbus.favorites

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.favorites.model.FavoriteSchedule
import com.matejdro.mbus.favorites.model.StopInfo
import com.matejdro.mbus.schedule.FakeScheduleRepository
import com.matejdro.mbus.schedule.model.Arrival
import com.matejdro.mbus.schedule.model.Line
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import java.time.LocalDateTime

class FavoritesRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val scheduleRepository = FakeScheduleRepository()

   private val db = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
      Database.Schema.create(it)
   }

   private val favoritesRepository = FavoritesRepositoryImpl(DbFavoriteQueries(db), scheduleRepository)

   private val today = LocalDateTime.of(2024, 3, 30, 9, 30)

   @Test
   fun `Create and fetch favorite`() = scope.runTest {
      favoritesRepository.createFavourite("Test")

      favoritesRepository.getListOfFavorites().test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Favorite(1, "Test", emptyList())
         )
      }
   }

   @Test
   fun `Sort favorites alphabetically`() = scope.runTest {
      favoritesRepository.createFavourite("C")
      favoritesRepository.createFavourite("A")

      favoritesRepository.getListOfFavorites().test {
         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Favorite(2, "A", emptyList()),
            Favorite(1, "C", emptyList())
         )
      }
   }

   @Test
   fun `Add stations to the favorite`() = scope.runTest {
      favoritesRepository.createFavourite("Test")

      favoritesRepository.getListOfFavorites().test {
         runCurrent()

         favoritesRepository.addStopToFavourite(1, 3)
         favoritesRepository.addStopToFavourite(1, 4)
         favoritesRepository.addStopToFavourite(1, 5)

         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Favorite(1, "Test", listOf(3, 4, 5))
         )
      }
   }

   @Test
   fun `Remove stations from the favorite`() = scope.runTest {
      favoritesRepository.createFavourite("Test")

      favoritesRepository.getListOfFavorites().test {
         runCurrent()

         favoritesRepository.addStopToFavourite(1, 3)
         favoritesRepository.addStopToFavourite(1, 4)
         favoritesRepository.addStopToFavourite(1, 5)

         favoritesRepository.removeStopToFavourite(1, 4)

         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Favorite(1, "Test", listOf(3, 5))
         )

         favoritesRepository.removeStopToFavourite(1, 3)
         favoritesRepository.removeStopToFavourite(1, 5)

         runCurrent()
         expectMostRecentItem() shouldBe listOf(
            Favorite(1, "Test", emptyList())
         )
      }
   }

   @Test
   @Suppress("LongMethod") // Test Data
   fun `Load schedule from all stations`() = scope.runTest {
      scheduleRepository.provideSchedule(
         77,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
         ),
      )

      scheduleRepository.provideSchedule(
         88,
         "Forest 88",
         "http://stopimage88.com",
         "Another stop in the forest",
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 3, 30, 9, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_6,
               LocalDateTime.of(2024, 3, 30, 12, 0),
               "MB -> Mesto"
            ),
         ),
      )

      favoritesRepository.createFavourite("Test")
      favoritesRepository.addStopToFavourite(1, 77)
      favoritesRepository.addStopToFavourite(1, 88)

      favoritesRepository.getScheduleForFavorite(1, today).data.test {
         runCurrent()

         expectMostRecentItem() shouldBeSuccessWithData FavoriteSchedule(
            Favorite(1, "Test", listOf(77, 88)),
            listOf(
               StopInfo(
                  77,
                  "Forest 77",
                  "A stop in the forest",
                  "http://stopimage.com"
               ),
               StopInfo(
                  88,
                  "Forest 88",
                  "Another stop in the forest",
                  "http://stopimage88.com"
               ),
            ),
            listOf(
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 9, 0),
                  "Forest 88\nMB -> Mesto"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_2,
                  LocalDateTime.of(2024, 3, 30, 10, 0),
                  "Forest 77\nMB -> Mesto"
               ),
               Arrival(
                  TEST_EXPECTED_LINE_6,
                  LocalDateTime.of(2024, 3, 30, 12, 0),
                  "Forest 88\nMB -> Mesto"
               ),
            ),
            listOf(
               TEST_EXPECTED_LINE_2,
               TEST_EXPECTED_LINE_6
            ),
            false,
         )
      }

      scheduleRepository.lastRequestedDate shouldBe today
   }

   @Test
   @Suppress("LongMethod") // Test Data
   fun `Filter lines`() = scope.runTest {
      scheduleRepository.provideSchedule(
         77,
         "Forest 77",
         "http://stopimage.com",
         "A stop in the forest",
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 3, 30, 10, 0),
               "MB -> Mesto"
            ),
         ),
      )

      scheduleRepository.provideSchedule(
         88,
         "Forest 88",
         "http://stopimage88.com",
         "Another stop in the forest",
         listOf(
            Arrival(
               TEST_EXPECTED_LINE_2,
               LocalDateTime.of(2024, 3, 30, 9, 0),
               "MB -> Mesto"
            ),
            Arrival(
               TEST_EXPECTED_LINE_6,
               LocalDateTime.of(2024, 3, 30, 12, 0),
               "MB -> Mesto"
            ),
         ),
      )

      favoritesRepository.createFavourite("Test")
      favoritesRepository.addStopToFavourite(1, 77)
      favoritesRepository.addStopToFavourite(1, 88)

      favoritesRepository.getScheduleForFavorite(1, today).data.test {
         runCurrent()

         favoritesRepository.setWhitelistedLines(1, setOf(6))
         runCurrent()

         expectMostRecentItem() shouldBeSuccessWithData FavoriteSchedule(
            Favorite(1, "Test", listOf(77, 88)),
            listOf(
               StopInfo(
                  77,
                  "Forest 77",
                  "A stop in the forest",
                  "http://stopimage.com"
               ),
               StopInfo(
                  88,
                  "Forest 88",
                  "Another stop in the forest",
                  "http://stopimage88.com"
               ),
            ),
            listOf(
               Arrival(
                  TEST_EXPECTED_LINE_6,
                  LocalDateTime.of(2024, 3, 30, 12, 0),
                  "Forest 88\nMB -> Mesto"
               ),
            ),
            listOf(
               TEST_EXPECTED_LINE_2,
               TEST_EXPECTED_LINE_6
            ),
            false,
         )
      }

      scheduleRepository.lastRequestedDate shouldBe today
   }
}

private val TEST_EXPECTED_LINE_2 = Line(2, "2", 0xFFFF0000.toInt())
private val TEST_EXPECTED_LINE_6 = Line(6, "6", 0xFF00FF00.toInt())
