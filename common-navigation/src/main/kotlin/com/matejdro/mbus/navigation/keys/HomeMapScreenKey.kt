package com.matejdro.mbus.navigation.keys

import android.location.Location
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class HomeMapScreenKey(
   @Transient
   val forcedLocation: Location? = null,
) : BaseScreenKey()
