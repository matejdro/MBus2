package com.matejdro.mbus.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
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
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.matejdro.mbus.location.toLatLng
import com.matejdro.mbus.navigation.keys.FavoriteListScreenKey
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import com.matejdro.mbus.navigation.keys.StopScheduleScreenKey
import com.matejdro.mbus.stops.model.Stop
import com.matejdro.mbus.ui.components.ErrorPopup
import com.matejdro.mbus.ui.debugging.FullScreenPreviews
import com.matejdro.mbus.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.exceptions.NoNetworkException
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

      val camera = rememberCameraPositionState(init = {
         val forcedLocation = key.forcedLocation
         this.position = if (forcedLocation != null) {
            CameraPosition.builder(DEFAULT_POSITION).target(forcedLocation.toLatLng()).build()
         } else {
            DEFAULT_POSITION
         }
      })

      ContentStateless(
         camera,
         isLocationGranted,
         data,
         navigateToFavorites = {
            navigator.navigateTo(FavoriteListScreenKey)
         },
         openStopSchedule = {
            navigator.navigateTo(StopScheduleScreenKey(it.id))
         },
         onCameraUpdate = viewModel::loadStops
      )

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

@Composable
private fun ContentStateless(
   camera: CameraPositionState,
   isLocationGranted: Boolean,
   data: Outcome<HomeState>?,
   navigateToFavorites: () -> Unit,
   onCameraUpdate: (LatLngBounds) -> Unit,
   openStopSchedule: (Stop) -> Unit,
) {
   val context = LocalContext.current
   val colorScheme = MaterialTheme.colorScheme

   val mapStyle = remember(colorScheme.background) {
      if (colorScheme.isDarkMode()) {
         null
      } else {
         MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
      }
   }

   Box {
      val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()

      Map(camera, isLocationGranted, mapStyle, backgroundColor, data?.mapData { it.stops }, onCameraUpdate, openStopSchedule)
      FavoritesButton(Modifier.safeDrawingPadding(), navigateToFavorites)

      if (data is Outcome.Progress) {
         CircularProgressIndicator(
            Modifier
               .padding(top = 20.dp)
               .safeDrawingPadding()
               .align(Alignment.TopCenter)
               .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
               .padding(8.dp)
         )
      } else if (data is Outcome.Error && data.exception is NoNetworkException) {
         Icon(
            painterResource(R.drawable.ic_no_internet),
            tint = MaterialTheme.colorScheme.error,
            contentDescription = stringResource(com.matejdro.mbus.ui.R.string.error_no_network),
            modifier = Modifier
               .padding(top = 16.dp)
               .safeDrawingPadding()
               .align(Alignment.TopCenter)
               .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
               .padding(8.dp)
               .alpha(HALF_LUMINANCE)
         )
      }

      ErrorPopup((data as? Outcome.Error)?.exception, data?.data != null)
   }
}

@Composable
private fun FavoritesButton(modifier: Modifier = Modifier, navigateToFavorites: () -> Unit) {
   ElevatedButton(
      onClick = navigateToFavorites,
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
   onCameraUpdate: (LatLngBounds) -> Unit,
   openStopSchedule: (Stop) -> Unit,
) {
   GoogleMap(
      cameraPositionState = camera,
      modifier = Modifier.fillMaxSize(),
      properties = MapProperties(isMyLocationEnabled = isLocationGranted, mapStyleOptions = mapStyle),
      googleMapOptionsFactory = {
         GoogleMapOptions().backgroundColor(backgroundColor).compassEnabled(false)
      },
      contentPadding = WindowInsets.safeDrawing.asPaddingValues()
   ) {
      UpdateModelOnCameraChange(camera, onCameraUpdate)

      val stops = data?.data.orEmpty()
      stops.forEachIndexed { index, stop ->
         key(index) {
            val state = rememberMarkerState()
            state.position = LatLng(stop.lat, stop.lon)

            Marker(
               state = state,
               title = stop.name,
               onClick = {
                  openStopSchedule(stop)
                  true
               }
            )
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

private fun ColorScheme.isDarkMode() = background.luminance() > HALF_LUMINANCE
private const val HALF_LUMINANCE = 0.5f
private const val MAP_BUTTON_ALPHA = 0.8f
private val DEFAULT_POSITION = CameraPosition(LatLng(46.55260772813225, 15.64425766468048), 16f, 0f, 0f)

@SuppressLint("UnrememberedMutableState")
@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun HomeMapScreenSuccessPreview() {
   PreviewTheme {
      ContentStateless(
         CameraPositionState(CameraPosition(LatLng(0.0, 0.0), 0f, 0f, 0f)),
         true,
         Outcome.Success(HomeState(emptyList(), null)),
         {},
         {},
         {}
      )
   }
}

@SuppressLint("UnrememberedMutableState")
@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun HomeMapScreenLoadingPreview() {
   PreviewTheme {
      ContentStateless(
         CameraPositionState(CameraPosition(LatLng(0.0, 0.0), 0f, 0f, 0f)),
         true,
         Outcome.Progress(),
         {},
         {},
         {}
      )
   }
}

@SuppressLint("UnrememberedMutableState")
@FullScreenPreviews
@Composable
internal fun HomeMapScreenNetworkErrorPreview() {
   PreviewTheme {
      ContentStateless(
         CameraPositionState(CameraPosition(LatLng(0.0, 0.0), 0f, 0f, 0f)),
         true,
         Outcome.Error(NoNetworkException()),
         {},
         {},
         {}
      )
   }
}
