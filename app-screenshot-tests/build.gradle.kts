plugins {
   androidLibraryModule
   compose
   alias(libs.plugins.paparazzi)
}

android {
   namespace = "com.matejdro.mbus.screenshottests"

   buildFeatures {
      androidResources = true
   }

   testOptions {
      unitTests.all {
         it.useJUnit()
      }
   }
}

dependencies {
   implementation(projects.app) {
      // If your app has multiple flavors, this is how you define them:
      //      attributes {
      //         attribute(
      //            ProductFlavorAttr.of("app"),
      //            objects.named(ProductFlavorAttr::class.java, "develop")
      //         )
      //      }
   }
   testImplementation(libs.junit4)
   testImplementation(libs.junit4.parameterinjector)
   testImplementation(libs.showkase)
}
