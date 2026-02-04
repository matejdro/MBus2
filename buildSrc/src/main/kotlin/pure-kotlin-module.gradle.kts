import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.squareup.anvil.plugin.AnvilExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.jvm")

   id("all-modules-commons")
}

afterEvaluate {
   if (pluginManager.hasPlugin("com.squareup.anvil")) {
      configure<AnvilExtension> {
         generateDaggerFactories.set(true)
      }
   }
}

val runDebugTestsTask = tasks.register("runDebugTests")
runDebugTestsTask.dependsOn(tasks.test)

val runDebugDetektTask = tasks.register("runDebugDetekt")
runDebugDetektTask.dependsOn("detektMain")
runDebugDetektTask.dependsOn("detektTest")

tasks.test {
   useJUnitPlatform()

   // Better test output
   systemProperty("kotest.assertions.collection.print.size", "300")
   systemProperty("kotest.assertions.collection.enumerate.size", "300")
}

dependencies {
}
