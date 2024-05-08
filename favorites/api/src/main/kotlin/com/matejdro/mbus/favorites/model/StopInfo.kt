package com.matejdro.mbus.favorites.model

data class StopInfo(
   val id: Int,
   val name: String,
   val description: String? = null,
   val imageUrl: String? = null,
)
