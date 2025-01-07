plugins {
   pureKotlinModule
   di
   moshi
   sqldelight
}

dependencies {
   api(projects.stops.api)

   implementation(projects.common)
   implementation(projects.commonRetrofit)
   implementation(projects.sharedDb)

   implementation(libs.androidx.datastore.core)
   implementation(libs.androidx.datastore.preferences.core)
   implementation(libs.dispatch)

   testImplementation(projects.stops.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.turbine)
}
