pluginManagement {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }
}

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

   repositories {
      mavenLocal()
      google()
      mavenCentral()
      maven("https://jitpack.io")
   }

   versionCatalogs {
      create("libs") {
         from(files("config/libs.toml"))
      }
   }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MBus2"

include(":app")
include(":app-screenshot-tests")
include(":common")
include(":common:test")
include(":common-android")
include(":common-android:test")
include(":common-compose")
include(":common-navigation")
include(":common-retrofit")
include(":common-retrofit:android")
include(":common-retrofit:test")
include(":favorites:api")
include(":favorites:data")
include(":favorites:test")
include(":favorites:ui")
include(":shared-db")
include(":shared-resources")
include(":shared-schedule")
include(":stops:api")
include(":stops:data")
include(":stops:test")
include(":schedule:api")
include(":schedule:data")
include(":schedule:test")
include(":schedule:ui")
include(":home:ui")
