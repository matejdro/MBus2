package com.matejdro.mbus.navigation.keys

import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.compose.result.ResultKey
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class AddToFavouritesDialogScreenKey(
   val stopId: Int,
   val stopName: String,
   val closeDialogReceiver: ResultKey<Unit>,
) : ScreenKey()
