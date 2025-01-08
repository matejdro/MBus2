plugins {
   pureKotlinModule
}

dependencies {
   api(projects.common)
   implementation(libs.kotlin.coroutines.test)
   implementation(libs.kotlin.coroutines)
   implementation(libs.dispatch.test)
   implementation(libs.kotest.assertions)
   implementation(libs.kotlinova.core)
   implementation(libs.turbine)
   implementation(libs.androidx.datastore.preferences.core)
}
