import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins.withId("com.android.library") {
   extensions.configure<LibraryExtension> {
      testFixtures.enable = true
   }
}

plugins.withId("com.android.application") {
   extensions.configure<ApplicationExtension> {
      testFixtures.enable = true
   }
}

plugins.withId("org.jetbrains.kotlin.jvm") {
   apply(plugin = "java-test-fixtures")
}

dependencies {
   add("testFixturesImplementation", libs.kotlin.coroutines.test)
   add("testFixturesImplementation", libs.turbine)
}
