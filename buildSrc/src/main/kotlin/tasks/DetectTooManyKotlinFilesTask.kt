package tasks

import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

@DisableCachingByDefault(because = "IO bound task")
open class DetectTooManyKotlinFilesTask : SourceTask() {
   @TaskAction
   fun execute() {
      val fileCount = source.files.count()
      if (fileCount > 20) {
         throw AssertionError(
            "App module has too many kotlin files ($fileCount). " +
               "This module is compiled for every build, so it should have as little code as possible. " +
               "Consider moving some code to other modules."
         )
      }
   }
}

fun Project.setupTooManyKotlinFilesTask() {
   tasks.register<DetectTooManyKotlinFilesTask>("detectTooManyFiles") {
      val kotlinExtension = project.extensions.getByType(KotlinSourceSetContainer::class.java)
      source(
         provider {
            kotlinExtension.sourceSets
               .flatMap { sourceSet ->
                  sourceSet.kotlin.srcDirs.filter {
                     // Filter out generated sources, such as KSP or Moshi sources and tests
                     val relativeFile = it.relativeTo(projectDir)
                     relativeFile.startsWith("src") &&
                        !it.path.contains("test", ignoreCase = true)
                  }
               }
         },
      )
      include("**/*.kt")
   }
}
