package com.matejdro.mbus.home

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import si.inova.kotlinova.navigation.screens.Screen

@OptIn(ExperimentalPermissionsApi::class)
class HomeMapScreen : Screen<HomeMapScreenKey>() {
   @Composable
   override fun Content(key: HomeMapScreenKey) {
      val isLocationGranted = requestLocationPermission()

      GoogleMap(
         modifier = Modifier.fillMaxSize(),
         properties = MapProperties(isMyLocationEnabled = isLocationGranted)
      )
   }

   @Composable
   private fun requestLocationPermission(): Boolean {
      val locationPermission = rememberMultiplePermissionsState(
         listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
         ),
      )

      LaunchedEffect(locationPermission.allPermissionsGranted) {
         if (!locationPermission.allPermissionsGranted) {
            locationPermission.launchMultiplePermissionRequest()
         }
      }

      val enableMyLocation = locationPermission.allPermissionsGranted
      return enableMyLocation
   }
}
