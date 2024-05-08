package com.matejdro.mbus.navigation.keys

import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class FavoriteScheduleScreenKey(val favoriteId: Long) : ScreenKey()
