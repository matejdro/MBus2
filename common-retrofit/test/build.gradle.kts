plugins {
   pureKotlinModule
   di
}

dependencies {
   api(projects.commonRetrofit)
   api(libs.okhttp.mockWebServer)
   api(libs.kotlinova.retrofit.test)

   implementation(projects.common.test)
   implementation(libs.kotlin.coroutines.test)
   implementation(libs.certificateTransparency)
}
