package com.matejdro.mbus.location

import android.location.Location

fun interface LocationProvider {
   suspend fun getUserLocation(): Location?
}
