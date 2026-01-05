import org.gradle.api.Project

/** Extension DSL for configuring which conventions to apply to subprojects */
open class GradlePatternExtension(private val project: Project) {

  /** List of conventions to apply to all subprojects (unless excluded) */
  val conventions = mutableListOf<String>()

  /** Projects to exclude from convention application */
  val excludedProjects = mutableSetOf<String>()

  /** Projects to explicitly include (if set, ONLY these get conventions) */
  val includedProjects = mutableSetOf<String>()

  /**
   * Map of project-specific convention overrides Key: project path, Value: list of convention names
   */
  val projectSpecificConventions = mutableMapOf<String, List<String>>()

  /** DSL: Apply these conventions to all subprojects */
  fun applyToAll(vararg conventionNames: String) {
    conventions.addAll(conventionNames)
  }

  /** DSL: Exclude specific projects from convention application */
  fun exclude(vararg projectPaths: String) {
    excludedProjects.addAll(projectPaths)
  }

  /** DSL: Only include specific projects for convention application */
  fun include(vararg projectPaths: String) {
    includedProjects.addAll(projectPaths)
  }

  /** DSL: Apply specific conventions to a specific project */
  fun forProject(projectPath: String, conventionNames: List<String>) {
    projectSpecificConventions[projectPath] = conventionNames
  }

  /** DSL: Convenience method for forProject */
  fun forProject(projectPath: String, vararg conventionNames: String) {
    forProject(projectPath, conventionNames.toList())
  }

  /** Check if a project should have conventions applied */
  fun shouldApplyTo(project: Project): Boolean {
    val projectPath = project.path

    // If includedProjects is set, ONLY apply to those
    if (includedProjects.isNotEmpty()) {
      return projectPath in includedProjects
    }

    // Otherwise, apply to all except excluded
    return projectPath !in excludedProjects
  }

  /** Get the list of conventions to apply to a specific project */
  fun getConventionsFor(project: Project): List<String> {
    val projectPath = project.path

    // Check for project-specific overrides first
    if (projectPath in projectSpecificConventions) {
      return projectSpecificConventions[projectPath]!!
    }

    // Otherwise return the default conventions
    return conventions
  }
}
