plugins {
   androidLibraryModule
   compose
   navigation
   parcelize
}

dependencies {
   api(libs.kotlinova.compose)
   api(libs.kotlinova.navigation)
   implementation(libs.androidx.activity.compose)
}
