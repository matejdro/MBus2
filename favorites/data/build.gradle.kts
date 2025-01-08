plugins {
   pureKotlinModule
   di
}

dependencies {
   api(projects.common)
   api(projects.favorites.api)
   api(projects.schedule.api)
   api(projects.sharedDb)

   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.sqldelight.async)
   implementation(libs.sqldelight.coroutines)

   testImplementation(projects.schedule.test)
   testImplementation(libs.sqldelight.jvm)
   testImplementation(libs.kotlinova.core.test)
}
