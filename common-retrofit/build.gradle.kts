plugins {
   pureKotlinModule

   di
   moshi
}

dependencies {
   api(libs.kotlinova.retrofit)

   implementation(libs.certificateTransparency)
   implementation(libs.dispatch)
   implementation(libs.okhttp)
   implementation(libs.retrofit.moshi)
   implementation(libs.kotlin.coroutines)

   implementation(projects.common)

   testImplementation(projects.common.test)
   testImplementation(projects.commonRetrofit.test)
   testImplementation(libs.turbine)
}
