plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.common)
   api(projects.commonRetrofit)
   api(libs.dispatch)
   api(libs.kotlinova.core)
   api(libs.kotlinova.retrofit)
   api(libs.okhttp)

   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.retrofit.moshi)
}
