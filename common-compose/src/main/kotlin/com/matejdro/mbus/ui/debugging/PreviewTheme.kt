package com.matejdro.mbus.ui.debugging

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import coil.Coil
import com.matejdro.mbus.ui.theme.MBusTheme
import si.inova.kotlinova.compose.preview.FakeCoilLoader
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.FakeAndroidDateTimeFormatter

@Composable
@Suppress("ModifierMissing") // This is intentional
fun PreviewTheme(
   formatter: AndroidDateTimeFormatter = FakeAndroidDateTimeFormatter(),
   fill: Boolean = true,
   content: @Composable () -> Unit,
) {
   Coil.setImageLoader(FakeCoilLoader())

   CompositionLocalProvider(LocalDateFormatter provides ComposeAndroidDateTimeFormatter(formatter)) {
      MBusTheme {
         Surface(modifier = if (fill) Modifier.fillMaxSize() else Modifier, content = content)
      }
   }
}
