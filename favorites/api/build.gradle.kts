plugins {
   pureKotlinModule
}

dependencies {
   api(projects.schedule.api)
   api(projects.stops.api)

   implementation(projects.common)
   implementation(libs.kotlin.coroutines)
}
