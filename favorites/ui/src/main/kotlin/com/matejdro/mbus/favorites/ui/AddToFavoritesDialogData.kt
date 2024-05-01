package com.matejdro.mbus.favorites.ui

import com.matejdro.mbus.favorites.model.Favorite

data class AddToFavoritesDialogData(
   val canClose: Boolean,
   val favorites: List<Favorite>,
)
