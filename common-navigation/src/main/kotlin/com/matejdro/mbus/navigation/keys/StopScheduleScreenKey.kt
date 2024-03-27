package com.matejdro.mbus.navigation.keys

import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class StopScheduleScreenKey(val stopId: Int) : ScreenKey()
