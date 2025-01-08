plugins {
   pureKotlinModule
}

dependencies {
   implementation(libs.kotlin.coroutines)
   api(libs.kotlinova.core)

   testImplementation(libs.turbine)
}
