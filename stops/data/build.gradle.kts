plugins {
   pureKotlinModule
   di
   moshi
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
   implementation(libs.sqldelight.async)
   implementation(libs.sqldelight.coroutines)
   implementation(libs.sqldelight.runtime)

   testImplementation(testFixtures(projects.common))
   testImplementation(libs.kotlinova.core.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.sqldelight.jvm)
   testImplementation(libs.turbine)
}
