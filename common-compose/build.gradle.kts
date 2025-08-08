plugins {
   androidLibraryModule
   compose
   parcelize
   showkase
}

android {
   namespace = "com.matejdro.mbus.ui"

   androidResources.enable = true
}

dependencies {
   api(libs.kotlinova.core)

   implementation(libs.coil)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.compose)
   implementation(libs.androidx.compose.material3.sizeClasses)
}
