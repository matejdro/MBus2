package com.matejdro.mbus.screenshottests

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import com.matejdro.mbus.showkase.getMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("JUnitMalformedDeclaration")
@RunWith(TestParameterInjector::class)
class ScreenshotTests {
   @get:Rule
   val paparazzi = Paparazzi(
      deviceConfig = PIXEL_5,
      theme = "android:Theme.Material.Light.NoActionBar",
      maxPercentDifference = 0.0,
      showSystemUi = false,
      renderingMode = SessionParams.RenderingMode.SHRINK,
   )

   object PreviewProvider : TestParameterValuesProvider() {
      override fun provideValues(context: Context?): List<TestKey> {
         return Showkase.getMetadata().componentList
            .filter { it.group != "Default Group" }
            .map { TestKey(it) }
      }
   }

   data class TestKey(val showkaseBrowserComponent: ShowkaseBrowserComponent) {
      override fun toString(): String {
         return showkaseBrowserComponent.componentKey
      }
   }

   @Test
   fun launchTests(
      @TestParameter(valuesProvider = PreviewProvider::class)
      testKey: TestKey,
   ) {
      val composable = @Composable {
         CompositionLocalProvider(LocalInspectionMode provides true) {
            testKey.showkaseBrowserComponent.component()
         }
      }

      paparazzi.snapshot {
         composable()
      }
      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            nightMode = NightMode.NIGHT
         )
      )
      paparazzi.snapshot("night") {
         composable()
      }
      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            ydpi = 600,
            xdpi = 300,
            screenWidth = 300 * 440 / 160,
            screenHeight = 600 * 440 / 160,
            nightMode = NightMode.NOTNIGHT
         )
      )
      paparazzi.snapshot("small") {
         composable()
      }
   }
}
