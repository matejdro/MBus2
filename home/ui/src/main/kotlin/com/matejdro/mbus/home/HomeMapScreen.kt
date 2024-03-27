package com.matejdro.mbus.home

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.Screen

@OptIn(ExperimentalPermissionsApi::class)
class HomeMapScreen(
   private val viewModel: HomeMapViewModel,
   private val navigator: Navigator,
) : Screen<HomeMapScreenKey>() {
   @Composable
   override fun Content(key: HomeMapScreenKey) {
      val isLocationGranted = requestLocationPermission()
      val data = viewModel.stops.collectAsStateWithLifecycleAndBlinkingPrevention().value

      val context = LocalContext.current
      val colorScheme = MaterialTheme.colorScheme

      val camera = rememberCameraPositionState(init = {
         this.position = DEFAULT_POSITION
      })

      val mapStyle = remember(colorScheme.background) {
         if (colorScheme.isDarkMode()) {
            null
         } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
         }
      }

      GoogleMap(
         cameraPositionState = camera,
         modifier = Modifier.fillMaxSize(),
         properties = MapProperties(isMyLocationEnabled = isLocationGranted, mapStyleOptions = mapStyle)
      ) {
         UpdateModelOnCameraChange(camera, viewModel::loadStops)

         val stops = data?.data.orEmpty()
         key(stops) {
            for (stop in stops) {
               key(stop.id) {
                  Marker(
                     state = MarkerState(LatLng(stop.lat, stop.lon)),
                     title = stop.name,
                     onClick = {
                        navigator.navigateTo(StopScheduleScreenKey(stop.id))
                        true
                     }
                  )
               }
            }
         }
      }
   }

   @OptIn(MapsComposeExperimentalApi::class)
   @Composable
   private fun UpdateModelOnCameraChange(
      camera: CameraPositionState,
      updateCamera: (LatLngBounds) -> Unit,
   ) {
      MapEffect(camera.position) {
         updateCamera(it.projection.visibleRegion.latLngBounds)
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
private val DEFAULT_POSITION = CameraPosition(LatLng(46.55260772813225, 15.64425766468048), 16f, 0f, 0f)
