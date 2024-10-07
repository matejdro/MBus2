package com.matejdro.mbus.location

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.toLatLng(): LatLng {
   return LatLng(latitude, longitude)
}
