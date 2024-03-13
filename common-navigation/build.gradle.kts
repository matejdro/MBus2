plugins {
   androidLibraryModule
   compose
   navigation
   parcelize
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.lifecycle.viewModel)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.dispatch)
   implementation(libs.kotlinova.compose)
}
