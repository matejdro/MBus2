plugins {
   pureKotlinModule
   moshi
   sqldelight
}

dependencies {
   api(projects.schedule.api)
   implementation(projects.common)
   implementation(projects.commonRetrofit)
   implementation(projects.sharedDb)

   implementation(libs.androidx.datastore.preferences.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testImplementation(libs.kotlinova.retrofit.test)
}
