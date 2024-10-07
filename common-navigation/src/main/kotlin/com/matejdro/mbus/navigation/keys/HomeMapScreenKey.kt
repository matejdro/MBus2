package com.matejdro.mbus.navigation.keys

import android.location.Location
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@Parcelize
data class HomeMapScreenKey(val forcedLocation: Location?) : ScreenKey()
