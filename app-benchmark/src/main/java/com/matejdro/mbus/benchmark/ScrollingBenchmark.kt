package com.matejdro.mbus.benchmark

import android.content.ComponentName
import android.content.Intent
import android.util.TypedValue
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.Rule
import org.junit.Test
import kotlin.math.roundToInt

/**
 * This is an example scrolling speed benchmark.
 *
 * It launches the default activity and scroll a bit.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 */
@Suppress("MagicNumber") // OK for benchmarks/tests
class ScrollingBenchmark {
   @get:Rule
   val benchmarkRule = MacrobenchmarkRule()

   @OptIn(ExperimentalMetricApi::class)
   @Test
   fun scrollAroundMap() {
      benchmarkRule.measureRepeated(
         packageName = "com.matejdro.mbus",
         // JIT compilation is the amount of JIT that our app needed. If this number gets high, we need to update baseline profile
         metrics = listOf(FrameTimingMetric(), TraceSectionMetric("JIT compiling %", TraceSectionMetric.Mode.Sum)),
         iterations = 5,
         startupMode = StartupMode.WARM,
         setupBlock = {
            val intent = Intent(Intent.ACTION_MAIN).apply {
               addCategory(Intent.CATEGORY_HOME)
               component = ComponentName(
                  "com.matejdro.mbus",
                  "com.matejdro.mbus.MainActivity"
               )

               putExtra("benchmark_forced_lat", 46.55260772813225)
               putExtra("benchmark_forced_lon", 15.64425766468048)
            }

            pressHome()
            startActivityAndWait(intent)
            device.wait(Until.hasObject(By.desc("Favorites")), 5_000).shouldNotBeNull()
         }
      ) {
         val screenStartOffset = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100f,
            InstrumentationRegistry.getInstrumentation().context.resources.displayMetrics
         ).roundToInt()

         swipeRight(screenStartOffset)
         Thread.sleep(500)

         swipeRight(screenStartOffset)
         Thread.sleep(500)

         swipeLeft(screenStartOffset)
         Thread.sleep(500)

         swipeLeft(screenStartOffset)
         Thread.sleep(500)

         swipeLeft(screenStartOffset)
         Thread.sleep(500)

         swipeLeft(screenStartOffset)
         Thread.sleep(500)
      }
   }

   private fun MacrobenchmarkScope.swipeLeft(screenStartOffset: Int) {
      device.swipe(
         /* startX = */ screenStartOffset,
         /* startY = */ screenStartOffset,
         /* endX = */ device.displayWidth - screenStartOffset,
         /* endY = */ screenStartOffset,
         /* steps = */ 5
      )
   }

   private fun MacrobenchmarkScope.swipeRight(screenStartOffset: Int) {
      device.swipe(
         /* startX = */ device.displayWidth - screenStartOffset,
         /* startY = */ screenStartOffset,
         /* endX = */ screenStartOffset,
         /* endY = */ screenStartOffset,
         /* steps = */ 5
      )
   }
}
