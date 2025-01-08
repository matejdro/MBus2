import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("checks")
   id("dependency-analysis")
}

dependencies {
   if (configurations.findByName("testImplementation") != null) {
      add("testImplementation", libs.junit5.api)
      add("testImplementation", libs.kotest.assertions)
      add("testImplementation", libs.kotlin.coroutines.test)
      add("testImplementation", libs.turbine)

      add("testRuntimeOnly", libs.junit5.engine)
   }
}
