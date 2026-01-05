import org.gradle.api.Plugin
import org.gradle.api.Project

/** Optimization convention plugin - Build and configuration cache setup */
class OptimizationConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      // Build cache configuration
      gradle.startParameter.isBuildCacheEnabled = true

      // FAST_BUILD mode - skip non-essential tasks at configuration time
      val fastBuildEnabled = providers.environmentVariable("FAST_BUILD").isPresent

      if (fastBuildEnabled) {
        // Disable documentation and lint tasks
        tasks.matching { task ->
          task.name.contains("javadoc", ignoreCase = true) ||
              task.name.contains("dokka", ignoreCase = true) ||
              task.name.contains("lint", ignoreCase = true)
        }.configureEach {
          enabled = false
        }

        logger.lifecycle("⚡ FAST BUILD MODE: Documentation and lint tasks disabled")
      }
    }
  }
}
