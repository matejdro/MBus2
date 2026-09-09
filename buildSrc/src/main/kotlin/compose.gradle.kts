import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.plugin.compose")
}

val stableClassesFile = project.layout.settingsDirectory.file("config/global-compose-stable-classes.txt")
composeCompiler {
   stabilityConfigurationFiles.add(stableClassesFile)
}

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
