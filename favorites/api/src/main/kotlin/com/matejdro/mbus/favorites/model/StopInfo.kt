package com.matejdro.mbus.favorites.model

import si.inova.kotlinova.core.data.Immutable

@Immutable
data class StopInfo(
   val id: Int,
   val name: String,
   val description: String? = null,
   val imageUrl: String? = null,
)
