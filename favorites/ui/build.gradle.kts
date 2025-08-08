plugins {
   androidLibraryModule
   di
   compose
   navigation
   showkase
}

android {
   namespace = "com.matejdro.mbus.favorites.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.common)
   api(projects.favorites.api)
   api(projects.schedule.api)
   api(libs.kotlinova.navigation)

   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(projects.sharedSchedule)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlinova.core)

   testImplementation(projects.favorites.test)
   testImplementation(libs.kotlinova.core.test)
}
