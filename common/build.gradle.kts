plugins {
   pureKotlinModule
   testFixtures
}

dependencies {
   implementation(libs.kotlin.coroutines)
   api(libs.kotlinova.core)

   testImplementation(libs.turbine)

   testFixturesImplementation(libs.dispatch.test)
   testFixturesImplementation(libs.kotest.assertions)
   testFixturesImplementation(libs.androidx.datastore.preferences.core)
}
