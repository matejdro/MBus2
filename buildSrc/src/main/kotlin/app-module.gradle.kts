import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.android.application")
   id("android-module-commons")
   id("kotlin-kapt")
   id("kotlinova")
   id("com.jraska.module.graph.assertion")
}

anvil {
   syncGeneratedSources.set(true)
}

kotlinova {
   tomlVersionBump {
      versionReportFiles.set(
         fileTree(rootDir).apply {
            include("**/build/dependencyUpdates/versions.json")
         }
      )

      tomlFile.set(File(rootDir, "config/libs.toml"))
   }
}

moduleGraphAssert {
   maxHeight = 6
   restricted = arrayOf(
      ":common-navigation -X> .*",
      ":(?!$name).* -X> .*:data",
      ":(?!$name).* -X> .*:ui",
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
   kaptAndroidTest(libs.dagger.compiler)
   androidTestImplementation(libs.androidx.test.runner)
}
