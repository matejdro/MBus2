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
   implementation(projects.schedule.api)

   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(libs.coil)
   implementation(libs.kotlinova.core)
}
