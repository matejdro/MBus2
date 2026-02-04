package com.matejdro.mbus.navigation.keys

import android.location.Location
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeMapScreenKey(val forcedLocation: Location?) : BaseScreenKey()
