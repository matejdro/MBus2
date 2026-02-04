plugins {
   pureKotlinModule
   testFixtures
}

dependencies {
   api(libs.kotlinova.core)
   api(projects.common)
   compileOnly(libs.androidx.compose.runtime)

   testFixturesApi(projects.common)
   testFixturesImplementation(testFixtures(projects.common))
}
