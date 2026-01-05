import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Root-only plugin that applies conventions to subprojects based on DSL configuration
 *
 * Usage in root build.gradle.kts:
 * ```
 * plugins {
 *     id("root-convention")
 * }
 *
 * gradlePatterns {
 *     // Apply these conventions to all subprojects
 *     applyToAll("kotlin-convention", "test-convention", "optimization-convention")
 *
 *     // Exclude specific projects
 *     exclude(":legacy-module", ":experimental")
 *
 *     // Or use include mode (ONLY these projects get conventions)
 *     // include(":core", ":api", ":domain")
 *
 *     // Project-specific convention overrides
 *     forProject(":spring-app", "kotlin-convention", "spring-convention", "test-convention")
 *     forProject(":android-app", "kotlin-convention", "android-convention")
 * }
 * ```
 */
class RootConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    // Ensure this is only applied to root project
    require(project == project.rootProject) {
      "RootConventionPlugin must be applied ONLY to the root project. " +
          "Current project: ${project.path}"
    }

    // Create the DSL extension
    val extension =
        project.extensions.create("gradlePatterns", GradlePatternExtension::class.java, project)

    // Configure subprojects after evaluation (when DSL is configured)
    project.afterEvaluate { configureSubprojects(project, extension) }
  }

  private fun configureSubprojects(rootProject: Project, extension: GradlePatternExtension) {
    rootProject.subprojects.forEach { subproject: Project ->
      // Check if this subproject should have conventions applied
      if (!extension.shouldApplyTo(subproject)) {
        rootProject.logger.lifecycle("⏭️  Skipping conventions for ${subproject.path} (excluded)")
        return@forEach
      }

      // Get the conventions to apply for this specific project
      val conventionsToApply = extension.getConventionsFor(subproject)

      if (conventionsToApply.isEmpty()) {
        rootProject.logger.lifecycle("⚠️  No conventions configured for ${subproject.path}")
        return@forEach
      }

      // Log what we're applying
      rootProject.logger.lifecycle(
          "✅ Applying conventions to ${subproject.path}: ${conventionsToApply.joinToString(", ")}",
      )

      // Apply each convention
      conventionsToApply.forEach { conventionId ->
        try {
          subproject.pluginManager.apply(conventionId)
        } catch (e: Exception) {
          rootProject.logger.error(
              "❌ Failed to apply convention '$conventionId' to ${subproject.path}: ${e.message}",
          )
          throw e
        }
      }
    }

    // Print summary
    printConfigurationSummary(rootProject, extension)
  }

  private fun printConfigurationSummary(rootProject: Project, extension: GradlePatternExtension) {
    rootProject.logger.lifecycle("")
    rootProject.logger.lifecycle("╔═══════════════════════════════════════════════════════════╗")
    rootProject.logger.lifecycle("║         GRADLE PATTERNS - CONFIGURATION SUMMARY          ║")
    rootProject.logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
    rootProject.logger.lifecycle(
        "║ Default Conventions: ${extension.conventions.joinToString(", ").padEnd(36)} ║",
    )

    if (extension.includedProjects.isNotEmpty()) {
      rootProject.logger.lifecycle("║ Mode: INCLUDE only                                       ║")
      rootProject.logger.lifecycle(
          "║ Included Projects: ${extension.includedProjects.size.toString().padEnd(38)} ║",
      )
    } else if (extension.excludedProjects.isNotEmpty()) {
      rootProject.logger.lifecycle("║ Mode: EXCLUDE specific                                   ║")
      rootProject.logger.lifecycle(
          "║ Excluded Projects: ${extension.excludedProjects.size.toString().padEnd(38)} ║",
      )
    } else {
      rootProject.logger.lifecycle("║ Mode: APPLY TO ALL                                       ║")
    }

    if (extension.projectSpecificConventions.isNotEmpty()) {
      rootProject.logger.lifecycle(
          "║ Project-Specific Overrides: ${
            extension.projectSpecificConventions.size.toString().padEnd(30)
          } ║",
      )
    }

    rootProject.logger.lifecycle("╚═══════════════════════════════════════════════════════════╝")
    rootProject.logger.lifecycle("")
  }
}
