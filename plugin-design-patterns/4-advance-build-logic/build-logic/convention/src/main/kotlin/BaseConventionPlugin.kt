import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories

/**
 * Base convention plugin for all enterprise modules. Applies common configuration to all projects.
 */
class BaseConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      group = "com.example.enterprise"

      // Configure repositories
      repositories {
        mavenCentral()
        google()
      }

      // Configure build cache if enabled
      if (hasProperty("buildCacheEnabled")) {
        configureBuildCache()
      }

      // Configure common tasks
      configureCommonTasks()
    }
  }

  private fun Project.configureBuildCache() {
    gradle.settingsEvaluated {
      buildCache {
        local {
          isEnabled = true
          directory = file("${rootDir}/.gradle/build-cache")
        }
      }
    }
  }

  private fun Project.configureCommonTasks() {
    tasks.register("projectInfo") {
      group = "help"
      description = "Displays project information"

      doLast {
        println("Project: $name")
        println("Group: $group")
        println("Version: $version")
        println("Build Dir: ${layout.buildDirectory.get()}")
      }
    }
  }
}
