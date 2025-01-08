plugins {
   androidLibraryModule
   compose
   showkase
}

android {
   namespace = "com.matejdro.mbus.schedule.shared"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.schedule.api)

   implementation(projects.commonCompose)
   implementation(libs.coil)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlinova.core)
}
