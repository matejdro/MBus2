package com.matejdro.mbus.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.tasks.await

@ContributesBinding(AppScope::class)
class LocationProviderImpl @Inject constructor(
   private val context: Context,
) : LocationProvider {
   private val client = LocationServices.getFusedLocationProviderClient(context)

   override suspend fun getUserLocation(): Location? {
      return client.lastLocation.await()
   }
}
