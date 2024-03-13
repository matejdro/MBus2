package com.matejdro.mbus.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.matejdro.mbus.navigation.keys.HomeMapScreenKey
import si.inova.kotlinova.navigation.screens.Screen

class HomeMapScreen : Screen<HomeMapScreenKey>() {
   @Composable
   override fun Content(key: HomeMapScreenKey) {
      Text("Hello World")
   }
}
