plugins {
   pureKotlinModule
}

dependencies {
   api(libs.kotlinova.core)
   api(projects.common)
   compileOnly(libs.androidx.compose.runtime)
}
