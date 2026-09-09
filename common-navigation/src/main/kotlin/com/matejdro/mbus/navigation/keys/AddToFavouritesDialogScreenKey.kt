package com.matejdro.mbus.navigation.keys

import kotlinx.serialization.Serializable
import si.inova.kotlinova.compose.result.ResultKey

@Serializable
data class AddToFavouritesDialogScreenKey(
   val stopId: Int,
   val stopName: String,
   val closeDialogReceiver: ResultKey<Unit>,
) : BaseScreenKey()
