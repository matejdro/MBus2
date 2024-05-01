package com.matejdro.mbus.favorites.model

import com.matejdro.mbus.sqldelight.generated.DbFavorite

fun DbFavorite.toFavorite(): Favorite {
   return Favorite(
      id = id,
      name = name,
      stopsIds = stops.split(",").mapNotNull { it.toIntOrNull() }
   )
}
