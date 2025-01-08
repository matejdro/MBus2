plugins {
   pureKotlinModule
   di
   moshi
   sqldelight
}

dependencies {
   api(projects.common)
   api(projects.commonRetrofit)
   api(projects.schedule.api)
   api(projects.sharedDb)
   api(projects.stops.api)
   api(libs.retrofit)

   implementation(libs.androidx.datastore.preferences.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testImplementation(projects.common.test)
   testImplementation(projects.stops.test)
   testImplementation(libs.kotlinova.core.test)
   testImplementation(libs.kotlinova.retrofit.test)
}
