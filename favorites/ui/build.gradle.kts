plugins {
   androidLibraryModule
   di
   compose
   navigation
   showkase
}

android {
   namespace = "com.matejdro.mbus.favorites.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(projects.favorites.api)

   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(projects.sharedSchedule)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testImplementation(projects.favorites.test)
}
