import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()
plugins {
   id("com.squareup.anvil")
}
anvil {
   trackSourceFiles = true
}
dependencies {
   add("implementation", libs.dagger.runtime)
}
