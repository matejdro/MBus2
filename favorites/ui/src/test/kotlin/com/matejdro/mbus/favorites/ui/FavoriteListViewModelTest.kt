package com.matejdro.mbus.favorites.ui

import com.matejdro.mbus.favorites.FakeFavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager

class FavoriteListViewModelTest {
   private val testScope = TestScope()
   private val favoritesRepository = FakeFavoritesRepository()

   private val viewModel = FavoriteListViewModel(testScope.testCoroutineResourceManager(), favoritesRepository, {})

   @Test
   fun `Load data`() = testScope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(
               3,
               "Favorite c",
               emptyList()
            ),
            Favorite(
               4,
               "Favorite d",
               emptyList()
            )
         )
      )

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.state.value shouldBeSuccessWithData listOf(
         Favorite(
            3,
            "Favorite c",
            emptyList()
         ),
         Favorite(
            4,
            "Favorite d",
            emptyList()
         )
      )
   }
}
