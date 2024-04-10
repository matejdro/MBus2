plugins {
   androidLibraryModule
   compose
   navigation
   showkase
}

android {
   namespace = "com.matejdro.mbus.schedule"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(projects.schedule.api)
   implementation(projects.stops.api)
   implementation(libs.coil)
   implementation(libs.kotlinova.core)

   testImplementation(projects.schedule.test)
   testImplementation(projects.stops.test)
}
