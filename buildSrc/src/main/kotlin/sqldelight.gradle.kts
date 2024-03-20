import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("app.cash.sqldelight")
}

afterEvaluate {
   tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
      // SQLDelight marks generated files incorrectly as regular source files. We must exclude it manually.
      // This is a workaround for the https://github.com/cashapp/sqldelight/issues/4157.
      exclude {
         it.file.absolutePath.contains("build/")
      }
   }
}

dependencies {
   add("implementation", libs.sqldelight.async)
   add("implementation", libs.sqldelight.coroutines)

   add("testImplementation", libs.sqldelight.jvm)
}
