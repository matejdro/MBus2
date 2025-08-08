plugins {
   pureKotlinModule
}

dependencies {
   api(projects.common)
   api(projects.schedule.api)

   compileOnly(libs.androidx.compose.runtime)
   implementation(libs.kotlin.coroutines)
}
