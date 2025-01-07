import com.android.build.api.dsl.LibraryBuildFeatures
import org.gradle.accessors.dm.LibrariesForLibs
import util.commonAndroid
import util.commonKotlinOptions

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.android")

   id("all-modules-commons")
   id("org.gradle.android.cache-fix")
}

val customConfig = extensions.create<CustomBuildConfiguration>("custom")

commonAndroid {
   // Use default namespace for no resources, modules that use resources must override this
   namespace = "com.matejdro.mbus.noresources"

   compileSdk = 34

   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17

      isCoreLibraryDesugaringEnabled = true
   }

   commonKotlinOptions {
      freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
      freeCompilerArgs += "-opt-in=kotlinx.coroutines.FlowPreview"
   }

   defaultConfig {
      minSdk = 24

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
   }

   testOptions {
      unitTests.all {
         it.useJUnitPlatform()

         // Better test output
         it.systemProperty("kotest.assertions.collection.print.size", "300")
         it.systemProperty("kotest.assertions.collection.enumerate.size", "300")
      }
   }

   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
   }

   buildFeatures {
      buildConfig = false
      resValues = false
      shaders = false

      if (this is LibraryBuildFeatures) {
         androidResources = false
      }
   }
}

kotlin {
   jvmToolchain(17)
}

detekt {
   config.from("$rootDir/config/detekt-android.yml")
}

dependencies {
   add("coreLibraryDesugaring", libs.desugarJdkLibs)
   add("detektPlugins", project(":detekt"))

   if (configurations.findByName("androidTestImplementation") != null) {
      add("androidTestImplementation", libs.kotest.assertions)
   }
}

// Even empty android test tasks take a while to execute. Disable all of them by default.
@Suppress("ComplexCondition") // It is just a properly commented list of tasks
tasks.configureEach {
   if (!customConfig.enableEmulatorTests.getOrElse(false) &&
      name.contains("AndroidTest", ignoreCase = true) &&
      !javaClass.name.startsWith("com.autonomousapps") && // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/945
      !name.contains("Lint", ignoreCase = true) // Android lint does not like disabling their tasks) {
   ) {
      enabled = false
   }
}
