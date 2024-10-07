package com.matejdro.mbus.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
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
import com.matejdro.mbus.location.toLatLng
import com.matejdro.mbus.navigation.keys.FavoriteListScreenKey
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.stops.model.Stop
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
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
      val isLocationGranted = if (key.forcedLocation == null) requestLocationPermission() else false
      val data = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention().value

      val context = LocalContext.current
      val colorScheme = MaterialTheme.colorScheme

      val camera = rememberCameraPositionState(init = {
         val forcedLocation = key.forcedLocation
         this.position = if (forcedLocation != null) {
            CameraPosition.builder(DEFAULT_POSITION).target(forcedLocation.toLatLng()).build()
         } else {
            DEFAULT_POSITION
         }
      })

      val mapStyle = remember(colorScheme.background) {
         if (colorScheme.isDarkMode()) {
            null
         } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
         }
      }

      Box {
         val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()

         Map(camera, isLocationGranted, mapStyle, backgroundColor, data?.mapData { it.stops })
         FavoritesButton(Modifier.safeDrawingPadding())
      }

      data?.data?.event?.let { HandleEvent(it, camera) }
   }

   @Composable
   private fun HandleEvent(event: HomeEvent, camera: CameraPositionState) {
      LaunchedEffect(event) {
         when (event) {
            is HomeEvent.MoveMap -> {
               camera.animate(CameraUpdateFactory.newLatLng(event.latLng))
               viewModel.notifyEventHandled()
            }
         }
      }
   }

   @Composable
   private fun FavoritesButton(modifier: Modifier = Modifier) {
      ElevatedButton(
         onClick = {
            navigator.navigateTo(FavoriteListScreenKey)
         },
         modifier
            .padding(16.dp)
            .size(40.dp)
            .alpha(MAP_BUTTON_ALPHA),
         shape = MaterialTheme.shapes.extraSmall,
         contentPadding = PaddingValues(4.dp),
         elevation = ButtonDefaults.elevatedButtonElevation(2.dp),
         colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
         )
      ) {
         Icon(
            painterResource(com.matejdro.mbus.shared_resources.R.drawable.ic_favorite),
            contentDescription = stringResource(com.matejdro.mbus.shared_resources.R.string.favorites)
         )
      }
   }

   @Composable
   @SuppressLint("UnrememberedMutableState") // Will fix as part of the issue #12
   private fun Map(
      camera: CameraPositionState,
      isLocationGranted: Boolean,
      mapStyle: MapStyleOptions?,
      backgroundColor: Int,
      data: Outcome<List<Stop>>?,
   ) {
      GoogleMap(
         cameraPositionState = camera,
         modifier = Modifier.fillMaxSize(),
         properties = MapProperties(isMyLocationEnabled = isLocationGranted, mapStyleOptions = mapStyle),
         googleMapOptionsFactory = {
            GoogleMapOptions().backgroundColor(backgroundColor)
         },
         contentPadding = WindowInsets.safeDrawing.asPaddingValues()
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
         )
      )

      val granted = locationPermission.permissions
         .first { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }
         .status == PermissionStatus.Granted

      LaunchedEffect(granted) {
         if (!granted) {
            locationPermission.launchMultiplePermissionRequest()
         }
      }

      rememberSaveable(granted) {
         if (granted) {
            viewModel.moveMapToUser()
         }
         true
      }

      val enableMyLocation = locationPermission.allPermissionsGranted
      return enableMyLocation
   }
}

private fun ColorScheme.isDarkMode() = background.luminance() > HALF_LUMINANCE
private const val HALF_LUMINANCE = 0.5f
private const val MAP_BUTTON_ALPHA = 0.8f
private val DEFAULT_POSITION = CameraPosition(LatLng(46.55260772813225, 15.64425766468048), 16f, 0f, 0f)
