plugins {
   pureKotlinModule
   moshi
}

dependencies {
   api(projects.stops.api)

   implementation(projects.common)
   implementation(projects.commonRetrofit)

   testImplementation(projects.stops.test)
}
