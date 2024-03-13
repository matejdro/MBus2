package com.matejdro.mbus.screenshottests

import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Before
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
      renderingMode = SessionParams.RenderingMode.SHRINK
   )

   object PreviewProvider : TestParameter.TestParameterValuesProvider {
      override fun provideValues(): List<TestKey> {
         // TODO uncomment this when you have at least one preview marked with @ShowkaseComposable
//         return Showkase.getMetadata().componentList
//            .filter { it.group != "Default Group" }
//            .map { TestKey(it) }

         return emptyList()
      }
   }

   data class TestKey(val showkaseBrowserComponent: ShowkaseBrowserComponent) {
      override fun toString(): String {
         return showkaseBrowserComponent.componentKey
      }
   }

   @Before
   fun setUp() {
      // Note: if you have lottie in your project, uncomment this
      // Workaround for the https://github.com/cashapp/paparazzi/issues/630
      // LottieTask.EXECUTOR = Executor(Runnable::run)
   }

   @Test
   fun launchTests(
      @TestParameter(valuesProvider = PreviewProvider::class)
      testKey: TestKey,
   ) {
      paparazzi.snapshot {
         testKey.showkaseBrowserComponent.component()
      }
   }
}
