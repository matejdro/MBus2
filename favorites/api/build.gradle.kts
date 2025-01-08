plugins {
   pureKotlinModule
}

dependencies {
   api(projects.common)
   api(projects.schedule.api)

   implementation(libs.kotlin.coroutines)
}
