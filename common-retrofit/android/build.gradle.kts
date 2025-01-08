plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.common)
   api(projects.commonRetrofit)
   api(libs.certificateTransparency.android)
   api(libs.dispatch)
   api(libs.kotlinova.core)
   api(libs.kotlinova.retrofit)
   api(libs.okhttp)

   implementation(libs.kotlin.coroutines)
}
