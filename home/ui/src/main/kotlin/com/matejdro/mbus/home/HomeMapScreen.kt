package com.matejdro.mbus.home

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
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

      val context = LocalContext.current
      val colorScheme = MaterialTheme.colorScheme

      val mapStyle = remember(colorScheme.background) {
         if (colorScheme.isDarkMode()) {
            null
         } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
         }
      }

      GoogleMap(
         modifier = Modifier.fillMaxSize(),
         properties = MapProperties(isMyLocationEnabled = isLocationGranted, mapStyleOptions = mapStyle)
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

private fun ColorScheme.isDarkMode() = background.luminance() > HALF_LUMINANCE
private const val HALF_LUMINANCE = 0.5f
