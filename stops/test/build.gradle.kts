plugins {
   pureKotlinModule
}

dependencies {
   api(projects.stops.api)

   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
}
