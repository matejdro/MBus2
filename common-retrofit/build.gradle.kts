plugins {
   pureKotlinModule
   moshi
}

dependencies {
   api(libs.kotlinova.retrofit)

   implementation(libs.certificateTransparency)
   implementation(libs.dispatch)
   implementation(libs.retrofit.moshi)
   implementation(libs.kotlin.coroutines)

   implementation(projects.common)

   testImplementation(projects.common.test)
   testImplementation(projects.commonRetrofit.test)
   testImplementation(libs.turbine)
}
