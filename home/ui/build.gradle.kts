plugins {
   androidLibraryModule
   compose
   navigation
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

   implementation(libs.accompanist.permissions)
   implementation(libs.googleMaps)
}
