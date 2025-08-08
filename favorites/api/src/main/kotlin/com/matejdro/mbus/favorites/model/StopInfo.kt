package com.matejdro.mbus.favorites.model

import androidx.compose.runtime.Immutable

@Immutable
data class StopInfo(
   val id: Int,
   val name: String,
   val description: String? = null,
   val imageUrl: String? = null,
)
