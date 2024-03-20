plugins {
   pureKotlinModule
   moshi
   sqldelight
}

sqldelight {
   databases {
      create("Database") {
         packageName.set("com.matejdro.mbus.stops.sqldelight.generated")
         schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
      }
   }
}

dependencies {
   api(projects.stops.api)

   implementation(projects.common)
   implementation(projects.commonRetrofit)

   implementation(libs.androidx.datastore.core)
   implementation(libs.androidx.datastore.preferences.core)
   implementation(libs.dispatch)

   testImplementation(projects.stops.test)
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.turbine)
}
