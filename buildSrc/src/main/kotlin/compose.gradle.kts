import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import util.commonAndroid
import util.commonKotlinOptions

val libs = the<LibrariesForLibs>()

plugins {
   id("com.joetr.compose.guard")
}

commonAndroid {
   buildFeatures {
      compose = true
   }
   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
   }
   commonKotlinOptions {
      freeCompilerArgs += listOf(
         "-P",
         "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" +
            "${rootDir.absolutePath}/config/global-compose-stable-classes.txt"
      )
   }
}

//region Compose Guard
composeGuardCheck {
   // Dynamic property detection is error prone on debug builds (and we are only building debug builds for PR changes)
   // See https://chrisbanes.me/posts/composable-metrics/#default-parameter-expressions-that-are-dynamic
   errorOnNewDynamicProperties = false

   // We don't care about unstable classes, only unstable Composables
   errorOnNewUnstableClasses = false

   // By default, all composables should be stable, so no need to generate baseline in most cases
   reportAllOnMissingBaseline = true
}

// Workaround for https://github.com/j-roskopf/ComposeGuard/issues/47 - manually register compose reports and metrics folder
composeGuard {
   configureKotlinTasks = false
}

// List of all tasks in this module that compile compose stuff (excluding KSP etc.)
val composeCompileTasks = listOf("compileDebugKotlin", "compileReleaseKotlin")

val composeReportsFolder = composeGuardCheck.outputDirectory.get()
project.tasks.named { composeCompileTasks.contains(it) }.withType<KotlinCompile>().configureEach {
   compilerOptions {
      freeCompilerArgs.addAll(
         "-P",
         "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            "$composeReportsFolder"
      )
      freeCompilerArgs.addAll(
         "-P",
         "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            "$composeReportsFolder"
      )
   }

   outputs.dir(composeReportsFolder)
}

tasks.register<Copy>("generateComposeGuardBaseline") {
   from(composeReportsFolder)
   into(composeGuardGenerate.outputDirectory)

   dependsOn("compileDebugKotlin")
}
//endregion

dependencies {
   add("implementation", libs.androidx.compose.ui)
   add("implementation", libs.androidx.compose.ui.graphics)
   add("implementation", libs.androidx.compose.ui.tooling.preview)
   add("implementation", libs.androidx.compose.ui.util)
   add("implementation", libs.androidx.compose.material3)
   add("implementation", libs.androidx.lifecycle.compose)

   add("debugRuntimeOnly", libs.androidx.compose.ui.test.manifest)
   add("debugImplementation", libs.androidx.compose.ui.tooling)
   add("debugImplementation", libs.rebugger)

   add("androidTestImplementation", libs.androidx.compose.ui.test.junit4)
}
