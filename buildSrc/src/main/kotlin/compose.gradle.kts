import org.gradle.accessors.dm.LibrariesForLibs
import util.commonAndroid

val libs = the<LibrariesForLibs>()

commonAndroid {
   buildFeatures {
      compose = true
   }
   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
   }
}

dependencies {
   add("implementation", libs.androidx.compose.ui)
   add("implementation", libs.androidx.compose.ui.graphics)
   add("implementation", libs.androidx.compose.ui.tooling.preview)
   add("implementation", libs.androidx.compose.ui.util)
   add("implementation", libs.androidx.compose.material3)
   add("implementation", libs.androidx.lifecycle.compose)
   add("implementation", libs.kotlinova.compose)

   add("debugImplementation", libs.androidx.compose.ui.test.manifest)
   add("debugImplementation", libs.androidx.compose.ui.tooling)

   add("androidTestImplementation", libs.androidx.compose.ui.test.junit4)
}
