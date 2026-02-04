package com.matejdro.mbus.navigation.keys

import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.compose.result.ResultKey

@Parcelize
data class AddToFavouritesDialogScreenKey(
   val stopId: Int,
   val stopName: String,
   val closeDialogReceiver: ResultKey<Unit>,
) : BaseScreenKey()
