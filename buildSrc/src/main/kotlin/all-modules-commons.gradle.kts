import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("checks")
   id("com.squareup.anvil")
   id("dependency-analysis")
}

anvil {
   trackSourceFiles = true
}

dependencies {
   add("implementation", libs.dagger.runtime)

   if (configurations.findByName("testImplementation") != null) {
      add("testImplementation", libs.junit5.api)
      add("testImplementation", libs.kotest.assertions)
      add("testImplementation", libs.kotlin.coroutines.test)
      add("testImplementation", libs.turbine)
      if (path != ":common:test") {
         add("testImplementation", project(":common:test"))
      }

      add("testRuntimeOnly", libs.junit5.engine)
   }
}
