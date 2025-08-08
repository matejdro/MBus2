import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val libs = the<LibrariesForLibs>()

plugins {
   id("checks")
   id("dependency-analysis")
}

configure<KotlinProjectExtension> {
   jvmToolchain(21)
}

dependencies {
   if (configurations.findByName("testImplementation") != null) {
      add("testImplementation", libs.junit5.api)
      add("testImplementation", libs.kotest.assertions)
      add("testImplementation", libs.kotlin.coroutines.test)
      add("testImplementation", libs.turbine)

      add("testRuntimeOnly", libs.junit5.engine)
      add("testRuntimeOnly", libs.junit5.launcher)
   }
}
