plugins {
   pureKotlinModule
   testFixtures

   di
   moshi
}

dependencies {
   api(projects.common)
   api(libs.kotlinova.retrofit)
   api(libs.kotlinova.core)
   api(libs.kotlinova.retrofit)
   api(libs.okhttp)
   api(libs.retrofit)

   implementation(libs.retrofit.moshi)
   implementation(libs.kotlin.coroutines)

   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.okhttp.mockWebServer)
   testImplementation(libs.turbine)

   testFixturesApi(libs.kotlinova.retrofit.test)
   testFixturesImplementation(libs.dagger.runtime)
   testFixturesImplementation(libs.kotlinova.core.test)
}
