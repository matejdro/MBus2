plugins {
   androidLibraryModule
   compose
   navigation
   unmock
}

android {
   namespace = "com.matejdro.mbus.home"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(projects.commonAndroid)
   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(projects.stops.api)

   implementation(libs.accompanist.permissions)
   implementation(libs.dispatch)
   implementation(libs.googleMaps)
   implementation(libs.kotlin.coroutines.playServices)
   implementation(libs.playServices.location)

   testImplementation(projects.stops.test)
}
