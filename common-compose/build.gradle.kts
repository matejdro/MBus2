plugins {
   androidLibraryModule
   compose
   parcelize
   showkase
}

android {
   namespace = "com.matejdro.mbus.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(libs.kotlinova.compose)
   implementation(projects.commonAndroid)
   implementation(projects.commonNavigation)
   implementation(libs.androidx.activity.compose)
   implementation(libs.coil)
   implementation(libs.androidx.compose.material3.sizeClasses)
}
