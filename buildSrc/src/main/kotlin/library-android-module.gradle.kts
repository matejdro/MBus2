import com.squareup.anvil.plugin.AnvilExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.android.library")
   id("android-module-commons")
}

afterEvaluate {
   if (pluginManager.hasPlugin("com.squareup.anvil")) {
      configure<AnvilExtension> {
         generateDaggerFactories.set(true)
      }
   }
}
