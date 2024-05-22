package com.matejdro.mbus.favorites.ui

import androidx.compose.runtime.Stable
import com.matejdro.mbus.common.logging.ActionLogger
import com.matejdro.mbus.favorites.FavoritesRepository
import com.matejdro.mbus.favorites.model.Favorite
import com.matejdro.mbus.navigation.keys.FavoriteListScreenKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import javax.inject.Inject

@Stable
class FavoriteListViewModel @Inject constructor(
   private val resources: CoroutineResourceManager,
   private val favoritesRepository: FavoritesRepository,
   private val actionLogger: ActionLogger,
) : SingleScreenViewModel<FavoriteListScreenKey>(resources.scope) {
   private val _state = MutableStateFlow<Outcome<List<Favorite>>>(Outcome.Progress())
   val state: StateFlow<Outcome<List<Favorite>>> = _state

   override fun onServiceRegistered() {
      actionLogger.logAction { "FavoriteListViewModel.onServiceRegistered()" }
      resources.launchResourceControlTask(_state) {
         emitAll(favoritesRepository.getListOfFavorites().map { Outcome.Success(it) })
      }
   }
}
