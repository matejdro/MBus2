plugins {
   pureKotlinModule
   testHelpers
}

dependencies {
   api(projects.schedule.api)

   implementation(libs.kotlin.coroutines)
}
