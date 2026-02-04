plugins {
   pureKotlinModule
   testFixtures
   moshi
}

dependencies {
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testFixturesImplementation(libs.kotlinova.core)
}
