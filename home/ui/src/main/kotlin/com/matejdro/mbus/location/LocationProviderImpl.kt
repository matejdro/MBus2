package com.matejdro.mbus.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.matejdro.mbus.common.di.ApplicationScope
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ContributesBinding(ApplicationScope::class)
class LocationProviderImpl @Inject constructor(
   private val context: Context,
) : LocationProvider {
   private val client = LocationServices.getFusedLocationProviderClient(context)

   override suspend fun getUserLocation(): Location? {
      return client.lastLocation.await()
   }
}
