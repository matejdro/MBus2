import org.gradle.accessors.dm.LibrariesForLibs
import tasks.setupTooManyKotlinFilesTask

val libs = the<LibrariesForLibs>()

plugins {
   id("com.android.application")
   id("android-module-commons")
   id("kotlin-kapt")
   id("kotlinova")
   id("com.jraska.module.graph.assertion")
   id("com.squareup.anvil")
}

anvil {
   syncGeneratedSources.set(true)
}

moduleGraphAssert {
   maxHeight = 6
   restricted = arrayOf(
      ":common-navigation -X> .*",

      // Prevent all modules but this app module from depending on :data and :ui modules
      ":(?!$name).* -X> .*:data",
      ":(?!$name).* -X> .*:ui",

      // Only allow common modules to depend on other common modules and shared resources
      ":common-.* -X> :(?!common).*",
   )
}

android {
   lint {
      checkDependencies = true
   }
}

dependencies {
   implementation(libs.dagger.runtime)
   kapt(libs.dagger.compiler)
}

project.setupTooManyKotlinFilesTask()
