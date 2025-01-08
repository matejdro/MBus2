plugins {
   pureKotlinModule
   testHelpers
}

dependencies {
   api(projects.common)
   api(projects.schedule.api)

   implementation(libs.kotlinova.core)
}
