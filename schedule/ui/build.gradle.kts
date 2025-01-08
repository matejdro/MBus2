plugins {
   androidLibraryModule
   compose
   di
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
   api(projects.common)
   api(projects.schedule.api)
   api(projects.stops.api)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.navigation)

   implementation(projects.commonCompose)
   implementation(projects.sharedSchedule)
   implementation(projects.sharedResources)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlinova.core)

   testImplementation(projects.schedule.test)
   testImplementation(projects.stops.test)
   testImplementation(libs.kotlinova.core.test)
}
