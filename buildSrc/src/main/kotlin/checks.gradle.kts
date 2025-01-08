import org.gradle.accessors.dm.LibrariesForLibs
import si.inova.kotlinova.gradle.KotlinovaExtension
import util.commonAndroid
import util.isAndroidProject

val libs = the<LibrariesForLibs>()

plugins {
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

dependencies {
   detektPlugins(libs.detekt.formatting)
   detektPlugins(libs.detekt.compilerWarnings)
   detektPlugins(libs.detekt.compose)
   detektPlugins(libs.kotlinova.navigation.detekt)
}
