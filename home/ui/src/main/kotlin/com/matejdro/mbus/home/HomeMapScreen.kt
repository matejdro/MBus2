package com.matejdro.mbus.home

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerState
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.Screen

@OptIn(ExperimentalPermissionsApi::class)
class HomeMapScreen(
   private val viewModel: HomeMapViewModel,
) : Screen<HomeMapScreenKey>() {
   @Composable
   override fun Content(key: HomeMapScreenKey) {
      val isLocationGranted = requestLocationPermission()
      val stops = viewModel.stops.collectAsStateWithLifecycleAndBlinkingPrevention().value

      GoogleMap(
         modifier = Modifier.fillMaxSize(),
         properties = MapProperties(isMyLocationEnabled = isLocationGranted)
      ) {
         for (stop in stops?.data.orEmpty()) {
            AdvancedMarker(
               state = MarkerState(LatLng(stop.lat, stop.lon)),
               title = stop.name
            )
         }
      }
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
