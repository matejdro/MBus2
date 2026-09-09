plugins {
   androidLibraryModule
   compose
   alias(libs.plugins.paparazzi)
}

android {
   namespace = "com.matejdro.mbus.screenshottests"

   testOptions {
      unitTests.all {
         it.useJUnitPlatform()

         val numSplits = 2 // How many TestsX classes are there
         it.maxParallelForks = minOf(Runtime.getRuntime().availableProcessors(), numSplits)
         it.systemProperty("numSplits", numSplits)
      }
   }
}

dependencyAnalysis {
   issues {
      onIncorrectConfiguration {
         // screenshot tests need to include app as implementation, otherwise resources do not work properly
         exclude(":app")
      }
   }
}

plugins.withId("app.cash.paparazzi") {
   // Defer until afterEvaluate so that testImplementation is created by Android plugin.
   afterEvaluate {
      dependencies.constraints {
         add("testImplementation", "com.google.guava:guava") {
            attributes {
               attribute(
                  TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                  objects.named(TargetJvmEnvironment::class, TargetJvmEnvironment.STANDARD_JVM)
               )
            }
            because(
               "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                  "See https://github.com/cashapp/paparazzi/issues/906."
            )
         }
      }
   }
}

dependencies {
   implementation(projects.app)
   testImplementation(libs.junit.params)
   testImplementation(libs.showkase)
}
