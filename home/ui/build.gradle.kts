plugins {
   androidLibraryModule
   di
   compose
   navigation
   unmock
   showkase
}

android {
   namespace = "com.matejdro.mbus.home"

   androidResources.enable = true
}

dependencies {
   api(projects.common)
   api(projects.stops.api)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)
   api(libs.kotlinova.navigation)

   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(libs.accompanist.permissions)
   implementation(libs.dispatch)
   implementation(libs.googleMaps)
   implementation(libs.kotlin.coroutines.playServices)
   implementation(libs.kotlinova.compose)
   implementation(libs.playServices.location)

   testImplementation(projects.stops.test)
   testImplementation(libs.kotlinova.core.test)
}
