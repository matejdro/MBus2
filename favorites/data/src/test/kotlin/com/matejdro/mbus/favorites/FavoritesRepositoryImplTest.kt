package com.matejdro.mbus.favorites

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.sqldelight.generated.Database
import com.matejdro.mbus.sqldelight.generated.DbFavoriteQueries
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider

class FavoritesRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()

   private val db = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
      Database.Schema.create(it)
   }

   private val favoritesRepository = FavoritesRepositoryImpl(DbFavoriteQueries(db))

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
}
