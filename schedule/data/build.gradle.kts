plugins {
   pureKotlinModule
   di
   moshi
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
   implementation(libs.sqldelight.async)
   implementation(libs.sqldelight.coroutines)
   implementation(libs.sqldelight.runtime)

   testImplementation(testFixtures(projects.common))
   testImplementation(testFixtures(projects.stops.api))
   testImplementation(libs.kotlinova.core.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.sqldelight.jvm)
}
