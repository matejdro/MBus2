plugins {
   `kotlin-dsl`
   alias(libs.plugins.detekt)
   // We must specify JVM plugin explicitly here to avoid version conflicts
   // It produces "Unsupported Kotlin plugin version" but it lets us compile
   // See https://slack-chats.kotlinlang.org/t/29177439/when-updating-kotlin-to-2-2-0-i-m-getting-https-github-com-t
   alias(libs.plugins.kotlin.jvm)
}

repositories {
   mavenLocal()
   google()
   mavenCentral()
   gradlePluginPortal()
}

detekt {
   config.setFrom("$projectDir/../config/detekt.yml", "$projectDir/../config/detekt-buildSrc.yml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
   reports {
      sarif.required.set(true)
   }
}

dependencies {
   implementation(libs.androidGradleCacheFix)
   implementation(libs.android.agp)
   implementation(libs.composeGuard)
   implementation(libs.anvil)
   implementation(libs.dependencyAnalysis)
   implementation(libs.detekt.plugin)
   implementation(libs.kotlin.plugin)
   implementation(libs.kotlin.plugin.compose)
   implementation(libs.kotlinova.gradle)
   implementation(libs.moduleGraphAssert)
   implementation(libs.moshi.ir)
   implementation(libs.orgJson)
   implementation(libs.sqldelight.gradle)
   implementation(libs.ksp)
   implementation(libs.tomlj)
   implementation(libs.unmock.plugin)

   // Workaround to have libs accessible (from https://github.com/gradle/gradle/issues/15383)
   compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

   detektPlugins(libs.detekt.formatting)
   detektPlugins(libs.detekt.compilerWarnings)
   detektPlugins(libs.detekt.compose)
}

tasks.register("pre-commit-hook", Copy::class) {
   from("$rootDir/../config/hooks/")
   into("$rootDir/../.git/hooks")
}

afterEvaluate {
   tasks.getByName("jar").dependsOn("pre-commit-hook")
}
