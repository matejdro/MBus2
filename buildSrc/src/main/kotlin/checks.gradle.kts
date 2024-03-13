import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.accessors.dm.LibrariesForLibs
import si.inova.kotlinova.gradle.KotlinovaExtension
import util.commonAndroid
import util.isAndroidProject

val libs = the<LibrariesForLibs>()

plugins {
   id("com.github.ben-manes.versions")
   id("io.gitlab.arturbosch.detekt")
   id("kotlinova")
}

if (isAndroidProject()) {
   commonAndroid {
      lint {
         lintConfig = file("$rootDir/config/android-lint.xml")
         abortOnError = true

         warningsAsErrors = true
         sarifReport = true
      }
   }
}

tasks.withType<DependencyUpdatesTask> {
   gradleReleaseChannel = "current"

   rejectVersionIf {
      candidate.version.contains("alpha", ignoreCase = true) ||
         candidate.version.contains("beta", ignoreCase = true) ||
         candidate.version.contains("RC", ignoreCase = true) ||
         candidate.version.contains("M", ignoreCase = true) ||
         candidate.version.contains("eap", ignoreCase = true) ||
         candidate.version.contains("dev", ignoreCase = true) ||
         candidate.version.contains("pre", ignoreCase = true)
   }

   reportfileName = "versions"
   outputFormatter = "json"
}

detekt {
   config.setFrom("$rootDir/config/detekt.yml")
}

configure<KotlinovaExtension> {
   mergeDetektSarif = true
   if (isAndroidProject()) {
      mergeAndroidLintSarif = true
   }

   enableDetektPreCommitHook = true
}

tasks.withType<com.android.build.gradle.internal.lint.AndroidLintTask>().configureEach {
   finalizedBy(":reportMerge")
}

dependencies {
   detektPlugins(libs.detekt.formatting)
   detektPlugins(libs.detekt.compilerWarnings)
   detektPlugins(libs.detekt.compose)
   detektPlugins(libs.kotlinova.navigation.detekt)
}
