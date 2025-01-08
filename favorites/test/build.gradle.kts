plugins {
   pureKotlinModule
   testHelpers
}

dependencies {
   api(projects.common)
   api(projects.favorites.api)
   api(projects.schedule.api)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
}
