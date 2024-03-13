plugins {
   pureKotlinModule
}

dependencies {
   implementation(libs.kotlin.coroutines)
   implementation(libs.dispatch)
   api(libs.kotlinova.core)

   testImplementation(projects.common.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.turbine)
}
