plugins {
   androidLibraryModule
   compose
}

android {
    namespace = "com.matejdro.mbus.${NAME}.ui"
    
    buildFeatures {
        androidResources = true
    }
}

dependencies {
    api(projects.${NAME}.api)
}
