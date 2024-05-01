plugins {
   pureKotlinModule
}

dependencies {
   api(projects.favorites.api)

   implementation(projects.common)
   implementation(projects.sharedDb)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.sqldelight.async)
   implementation(libs.sqldelight.coroutines)

   testImplementation(projects.schedule.test)
   testImplementation(libs.sqldelight.jvm)
}
