package com.matejdro.mbus.favorites.ui

import com.matejdro.mbus.favorites.FakeFavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager

class AddToFavoritesDialogViewModelTest {
   private val scope = TestScope()
   private val favoritesRepository = FakeFavoritesRepository()

   private val viewModel = AddToFavoritesDialogViewModel(scope.testCoroutineResourceManager(), favoritesRepository)

   @Test
   fun `Load data`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(1, "A", listOf(2, 3)),
            Favorite(2, "B", listOf(4, 5)),
         )
      )

      viewModel.load(6)
      runCurrent()

      viewModel.data.value shouldBeSuccessWithData AddToFavoritesDialogData(
         false,
         listOf(
            Favorite(1, "A", listOf(2, 3)),
            Favorite(2, "B", listOf(4, 5)),
         )
      )
   }

   @Test
   fun `Do not show favorites that already contain this stop`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(1, "A", listOf(2, 3)),
            Favorite(2, "B", listOf(5, 6)),
         )
      )

      viewModel.load(6)
      runCurrent()

      viewModel.data.value shouldBeSuccessWithData AddToFavoritesDialogData(
         false,
         listOf(
            Favorite(1, "A", listOf(2, 3)),
         )
      )
   }

   @Test
   fun `Selecting favorite should add it to the favorite's stop list`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(1, "A", listOf(2, 3)),
            Favorite(2, "B", listOf(5, 6)),
         )
      )

      viewModel.load(4)
      runCurrent()
      viewModel.select(1)
      runCurrent()

      favoritesRepository.getListOfFavorites().first() shouldBe listOf(
         Favorite(1, "A", listOf(2, 3, 4)),
         Favorite(2, "B", listOf(5, 6)),
      )
   }

   @Test
   fun `Selecting favorite should emit canClose=true`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(1, "A", listOf(2, 3)),
            Favorite(2, "B", listOf(5, 6)),
         )
      )

      viewModel.load(4)
      runCurrent()
      viewModel.select(1)
      runCurrent()

      viewModel.data.value shouldBeSuccessWithData AddToFavoritesDialogData(true, emptyList())
   }

   @Test
   fun `Creating new favorite should create it add it to the new favorite's stop list`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(0, "A", listOf(2, 3)),
            Favorite(1, "B", listOf(5, 6)),
         )
      )

      viewModel.load(4)
      viewModel.addAndSelect("C")
      runCurrent()

      favoritesRepository.getListOfFavorites().first() shouldBe listOf(
         Favorite(0, "A", listOf(2, 3)),
         Favorite(1, "B", listOf(5, 6)),
         Favorite(2, "C", listOf(4)),
      )
   }

   @Test
   fun `Creating new favorite should emit canClose=true`() = scope.runTest {
      favoritesRepository.setFavorites(
         listOf(
            Favorite(0, "A", listOf(2, 3)),
            Favorite(1, "B", listOf(5, 6)),
         )
      )

      viewModel.load(4)
      viewModel.addAndSelect("C")
      runCurrent()

      viewModel.data.value shouldBeSuccessWithData AddToFavoritesDialogData(true, emptyList())
   }
}
