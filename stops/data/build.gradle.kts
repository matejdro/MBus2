plugins {
   pureKotlinModule
   di
   moshi
   sqldelight
}

dependencies {
   api(projects.common)
   api(projects.commonRetrofit)
   api(projects.sharedDb)
   api(projects.stops.api)
   api(libs.androidx.datastore.core)
   api(libs.kotlin.coroutines)
   api(libs.kotlinova.core)
   api(libs.retrofit)

   implementation(libs.androidx.datastore.preferences.core)
   implementation(libs.dispatch)

   testImplementation(projects.common.test)
   testImplementation(libs.kotlinova.core.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.turbine)
}
