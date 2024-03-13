import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.jvm")

   id("all-modules-commons")
}

anvil {
   generateDaggerFactories.set(true)
}

tasks.withType(KotlinCompilationTask::class.java) {
   compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
   compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.coroutines.FlowPreview")
}

tasks.test {
   useJUnitPlatform()

   // Better test output
   systemProperty("kotest.assertions.collection.print.size", "300")
   systemProperty("kotest.assertions.collection.enumerate.size", "300")
}

dependencies {
}
