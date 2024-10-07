plugins {
   id("com.android.test")
   commonAndroid
}

android {
   namespace = "com.matejdro.mbus.benchmark"

   buildTypes {
      create("benchmark") {
         isDebuggable = true
         signingConfig = getByName("debug").signingConfig
         matchingFallbacks += listOf("release")
      }
   }

   defaultConfig {
      testInstrumentationRunnerArguments["androidx.benchmark.perfettoSdkTracing.enable"] = "true"
   }

   targetProjectPath = ":app"
   experimentalProperties["android.experimental.self-instrumenting"] = true
}

custom {
   enableEmulatorTests = true
}

dependencies {
   implementation(libs.junit4)
   implementation(libs.androidx.test.espresso)
   implementation(libs.androidx.test.uiautomator)
   implementation(libs.androidx.benchmark.macro.junit4)
   implementation(libs.androidx.profileInstaller)
   implementation(libs.androidx.perfetto)
   implementation(libs.androidx.perfetto.binary)
   implementation(libs.kotest.assertions)
}

androidComponents {
   beforeVariants(selector().all()) {
      it.enable = it.buildType == "benchmark"
   }
}
