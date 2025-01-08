import nl.littlerobots.vcu.plugin.resolver.ModuleVersionCandidate
import nl.littlerobots.vcu.plugin.versionSelector

// Please do not add any subprojects {} / allprojects {} blocks or anything else that affects suborpojects to allow for
// project isolation when it comes out (https://gradle.github.io/configuration-cache/#project_isolation)

plugins {
   id("com.autonomousapps.dependency-analysis")
   alias(libs.plugins.versionCatalogUpdate)
}

dependencyAnalysis {
   structure {
      ignoreKtx(true)

      bundle("coil") {
         // We only ever want coil-compose, so coil is considered as a group
         includeGroup("io.coil-kt")
      }

      bundle("compose") {
         // Compose libraries are blanket-included to for convenience. It shouldn't cause a big issue
         includeGroup("androidx.compose.animation")
         includeGroup("androidx.compose.foundation")
         includeGroup("androidx.compose.material")
         includeGroup("androidx.compose.material3")
         includeGroup("androidx.compose.runtime")
         includeGroup("androidx.compose.ui")
      }

      // Library Groups:

      bundle("androidxActivity") {
         includeGroup("androidx.activity")
      }

      bundle("androidxCore") {
         includeGroup("androidx.core")
      }

      bundle("androidxLifecycle") {
         includeGroup("androidx.lifecycle")
      }

      bundle("inject") {
         includeGroup("me.tatarka.inject")
         includeGroup("software.amazon.lastmile.kotlin.inject.anvil")
      }

      bundle("datastore") {
         includeGroup("androidx.datastore")
      }

      bundle("kotest") {
         includeGroup("io.kotest")
      }

      bundle("showkase") {
         includeGroup("com.airbnb.android")
      }

      bundle("sqlDelight") {
         includeGroup("app.cash.sqldelight")
      }

      bundle("dagger") {
         includeGroup("com.google.dagger")
         includeGroup("com.squareup.anvil")
         includeDependency("javax.inject:javax.inject")
      }

      bundle("maps") {
         includeGroup("com.google.maps.android")
         includeDependency("com.google.android.gms:play-services-maps")
      }

      bundle("certificate transparency") {
         includeGroup("com.appmattus.certificatetransparency")
      }

      bundle("okhttp and okio") {
         includeGroup("com.squareup.okhttp3")
         includeGroup("com.squareup.okio")
      }
   }
}

versionCatalogUpdate {
   catalogFile.set(file("config/libs.toml"))

   fun ModuleVersionCandidate.newlyContains(keyword: String): Boolean {
      return !currentVersion.contains(keyword, ignoreCase = true) && candidate.version.contains(keyword, ignoreCase = true)
   }

   versionSelector {
      !it.newlyContains("alpha") &&
         !it.newlyContains("beta") &&
         !it.newlyContains("RC") &&
         !it.newlyContains("M") &&
         !it.newlyContains("eap") &&
         !it.newlyContains("dev") &&
         !it.newlyContains("pre")
   }
}

// Always update to the ALL distribution when updating Gradle
tasks.wrapper {
   distributionType = Wrapper.DistributionType.ALL
}
