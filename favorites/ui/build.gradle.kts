plugins {
   androidLibraryModule
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

   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testImplementation(projects.favorites.test)
}
