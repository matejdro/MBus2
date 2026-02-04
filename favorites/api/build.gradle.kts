plugins {
   pureKotlinModule
   testFixtures
}

dependencies {
   api(projects.common)
   api(projects.schedule.api)

   compileOnly(libs.androidx.compose.runtime)
   implementation(libs.kotlin.coroutines)

   testFixturesApi(projects.common)
   testFixturesImplementation(testFixtures(projects.common))
   testFixturesApi(projects.schedule.api)
   testFixturesImplementation(libs.kotlinova.core)
}
