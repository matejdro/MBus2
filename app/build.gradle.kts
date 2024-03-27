import com.android.build.api.variant.BuildConfigField
import java.io.FileNotFoundException

plugins {
   androidAppModule
   compose
   navigation
   parcelize
   showkase
   sqldelight

   alias(libs.plugins.playServices)
   alias(libs.plugins.crashlytics)
}

android {
   namespace = "com.matejdro.mbus"

   buildFeatures {
      buildConfig = true
   }

   defaultConfig {
      applicationId = "com.matejdro.mbus"
      targetSdk = 33
      versionCode = 1
      versionName = "1.0"

      testInstrumentationRunner = "com.matejdro.mbus.instrumentation.TestRunner"
      testInstrumentationRunnerArguments += "clearPackageData" to "true"

      androidComponents {
         onVariants {
            it.buildConfigFields.put(
               "GIT_HASH",
               gitVersionProvider.flatMap { task ->
                  task.gitVersionOutputFile.map { file ->
                     val gitHash = try {
                        file.asFile.readText(Charsets.UTF_8)
                     } catch (e: FileNotFoundException) {
                        // See https://github.com/gradle/gradle/issues/19252
                        throw IllegalStateException(
                           "Failed to load git configuration. " +
                              "Please disable configuration cache for this one build and try again",
                           e
                        )
                     }

                     BuildConfigField(
                        "String",
                        "\"$gitHash\"",
                        "Git Version"
                     )
                  }
               }
            )
         }
      }
   }

   testOptions {
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
   }

   if (hasProperty("testAppWithProguard")) {
      testBuildType = "proguardedDebug"
   }

   signingConfigs {
      getByName("debug") {
         // SHA1: 86:13:26:CC:8B:85:75:A5:27:22:94:D1:DD:FF:92:20:7B:F4:A7:3C
         // SHA256: 60:0E:71:18:AC:69:9F:DD:4B:D2:38:82:D7:F8:72:C9:8C:A5:64:C4:B0:0D:F4:87:F2:72:FA:F7:28:EA:72:B6

         storeFile = File(rootDir, "keys/debug.jks")
         storePassword = "android"
         keyAlias = "androiddebugkey"
         keyPassword = "android"
      }

      create("release") {
         // SHA1: 8B:81:B2:CF:75:EF:85:FF:89:42:09:A6:35:99:72:FB:16:93:E3:62
         // SHA256: FF:44:51:CD:EC:97:19:77:D1:F1:EF:ED:0C:F3:97:35:9D:09:72:8E:19:26:7B:AA:64:A0:3E:C6:1A:C7:A2:A4

         storeFile = File(rootDir, "keys/release.jks")
         storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
         keyAlias = "app"
         keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
      }
   }

   buildTypes {
      getByName("debug") {
         signingConfig = signingConfigs.getByName("debug")
      }

      create("proguardedDebug") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
            "proguard-rules-test.pro"
         )

         testProguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
            "proguard-rules-test.pro"
         )

         matchingFallbacks += "debug"

         signingConfig = signingConfigs.getByName("debug")
      }

      create("benchmark") {
         isDebuggable = true
         initWith(buildTypes.getByName("release"))
         signingConfig = signingConfigs.getByName("debug")
         matchingFallbacks += listOf("release")
      }

      getByName("release") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
         )

         signingConfig = signingConfigs.getByName("release")
      }
   }
}

sqldelight {
   databases {
      create("Database") {
         packageName.set("com.matejdro.mbus")
         dependency(projects.sharedDb)
      }
   }
}

dependencies {
   implementation(projects.commonAndroid)
   implementation(projects.commonNavigation)
   implementation(projects.commonRetrofit.android)
   implementation(projects.commonCompose)
   implementation(projects.home.ui)
   implementation(projects.sharedDb)
   implementation(projects.stops.data)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.core.splashscreen)
   implementation(libs.androidx.lifecycle.runtime)
   implementation(libs.androidx.lifecycle.viewModel)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.certificateTransparency)
   implementation(libs.firebase.crashlytics)
   implementation(libs.dispatch)
   implementation(libs.kotlin.immutableCollections)
   implementation(libs.retrofit.moshi)
   implementation(libs.simpleStack)
   implementation(libs.sqldelight.android)

   implementation(libs.androidx.datastore)
   implementation(libs.androidx.datastore.preferences)

   testImplementation(projects.commonAndroid.test)
   androidTestImplementation(projects.commonAndroid.test)
   androidTestImplementation(projects.commonRetrofit.test)
   androidTestImplementation(libs.dispatch.espresso)
   androidTestImplementation(libs.kotlinova.retrofit.test)
   androidTestImplementation(libs.kotlinova.compose.androidTest)
   androidTestImplementation(libs.androidx.test.runner)
   androidTestUtil(libs.androidx.test.orchestrator)
   testImplementation(libs.junit4)

   add("benchmarkImplementation", libs.androidx.profileInstaller)
   add("benchmarkImplementation", libs.androidx.compose.tracing)
}

abstract class GitVersionTask : DefaultTask() {
   @get:OutputFile
   abstract val gitVersionOutputFile: RegularFileProperty

   @TaskAction
   fun taskAction() {
      val gitProcess = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
      val error = gitProcess.errorStream.readBytes().decodeToString()
      if (error.isNotBlank()) {
         error("Git error : $error")
      }

      val gitVersion = gitProcess.inputStream.readBytes().decodeToString().trim()

      gitVersionOutputFile.get().asFile.writeText(gitVersion)
   }
}

val gitVersionProvider = tasks.register<GitVersionTask>("gitVersionProvider") {
   val targetFile = File(project.layout.buildDirectory.asFile.get(), "intermediates/gitVersionProvider/output")

   targetFile.also {
      it.parentFile.mkdirs()
      gitVersionOutputFile.set(it)
   }
   outputs.upToDateWhen { false }
}
