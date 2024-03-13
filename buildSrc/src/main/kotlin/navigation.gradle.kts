import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.squareup.anvil")
}

dependencies {
   if (name != "common-navigation") {
      add("implementation", project(":common-navigation"))
   }

   anvil(libs.kotlinova.navigation.compiler)

   add("testImplementation", libs.kotlinova.navigation.test)
   add("androidTestImplementation", libs.kotlinova.navigation.test)
}
