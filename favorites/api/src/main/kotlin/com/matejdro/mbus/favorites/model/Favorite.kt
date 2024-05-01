package com.matejdro.mbus.favorites.model

data class Favorite(
   val id: Long,
   val name: String,
   val stopsIds: List<Int>,
)
