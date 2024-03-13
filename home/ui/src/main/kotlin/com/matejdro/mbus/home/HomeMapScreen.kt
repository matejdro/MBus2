package com.matejdro.mbus.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import si.inova.kotlinova.navigation.screens.Screen

class HomeMapScreen : Screen<HomeMapScreenKey>() {
   @Composable
   override fun Content(key: HomeMapScreenKey) {
      GoogleMap(modifier = Modifier.fillMaxSize())
   }
}
