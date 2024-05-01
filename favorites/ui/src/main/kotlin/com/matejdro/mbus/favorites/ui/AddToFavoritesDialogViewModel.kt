package com.matejdro.mbus.favorites.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.favorites.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.CoroutineScopedService
import javax.inject.Inject

@Stable
class AddToFavoritesDialogViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val favoritesRepository: FavoritesRepository,
) : CoroutineScopedService(resources.scope) {
   private val _data = MutableStateFlow<Outcome<AddToFavoritesDialogData>>(Outcome.Progress())
   val data: StateFlow<Outcome<AddToFavoritesDialogData>> = _data

   private var lastLoadedStop: Int? = null

   fun load(stopId: Int) = resources.launchResourceControlTask(_data) {
      lastLoadedStop = stopId

      emitAll(
         favoritesRepository.getListOfFavorites().map { list ->
            Outcome.Success(
               AddToFavoritesDialogData(
                  false,
                  list.filter {
                     !it.stopsIds.contains(stopId)
                  }
               )
            )
         }
      )
   }

   fun select(favoriteId: Long) {
      val stopId = lastLoadedStop ?: return

      resources.launchResourceControlTask(_data) {
         favoritesRepository.addStopToFavourite(favoriteId, stopId)
         emit(Outcome.Success(AddToFavoritesDialogData(true, emptyList())))
      }
   }

   fun addAndSelect(name: String) {
      val stopId = lastLoadedStop ?: return

      resources.launchResourceControlTask(_data) {
         val favoriteId = favoritesRepository.createFavourite(name)
         favoritesRepository.addStopToFavourite(favoriteId, stopId)
         emit(Outcome.Success(AddToFavoritesDialogData(true, emptyList())))
      }
   }
}
