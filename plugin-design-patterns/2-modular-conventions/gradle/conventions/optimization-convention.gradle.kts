/**
 * Build optimization convention Apply with: apply(from =
 * "$rootDir/gradle/conventions/optimization-convention.gradle.kts")
 */

// Build cache configuration
gradle.startParameter.apply {
  isBuildCacheEnabled = true
  isConfigurationCacheEnabled =
      providers.gradleProperty("configuration.cache").map { it.toBoolean() }.orElse(false).get()
}

// Task graph optimization
gradle.taskGraph.whenReady {
  val isDebugMode =
      providers
          .gradleProperty("debug.task.skip")
          .map { it.toBoolean() }
          .orElse(providers.environmentVariable("DEBUG_BUILD").map { it.toBoolean() })
          .getOrElse(false)

  if (providers.environmentVariable("FAST_BUILD").isPresent) {
    val skippedTasks = mutableListOf<String>()
    val taskCategories = mutableMapOf<String, Int>()

    allTasks.forEach { task ->
      val shouldSkip =
          when {
            task.name.contains("javadoc", ignoreCase = true) -> {
              taskCategories["JavaDoc"] = taskCategories.getOrDefault("JavaDoc", 0) + 1
              true
            }

            task.name.contains("sources", ignoreCase = true) -> {
              taskCategories["Sources"] = taskCategories.getOrDefault("Sources", 0) + 1
              true
            }

            task.name.contains("dokka", ignoreCase = true) -> {
              taskCategories["Documentation"] = taskCategories.getOrDefault("Documentation", 0) + 1
              true
            }

            task.name.contains("lint", ignoreCase = true) -> {
              taskCategories["Linting"] = taskCategories.getOrDefault("Linting", 0) + 1
              true
            }

            task.name.contains("ktlint", ignoreCase = true) -> {
              taskCategories["KtLint"] = taskCategories.getOrDefault("KtLint", 0) + 1
              true
            }

            else -> false
          }

      if (shouldSkip) {
        task.enabled = false
        skippedTasks.add("${task.project.name}:${task.name}")
      }
    }

    // Report skipped tasks
    if (skippedTasks.isNotEmpty()) {
      if (isDebugMode) {
        logger.lifecycle("╔═══════════════════════════════════════════════════════════╗")
        logger.lifecycle("║           FAST BUILD MODE - TASK SKIP REPORT             ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
        logger.lifecycle("║ Total tasks skipped: ${skippedTasks.size.toString().padEnd(36)} ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")

        taskCategories.forEach { (category, count) ->
          val line = "║ $category: ${count.toString().padEnd(48 - category.length)} ║"
          logger.lifecycle(line)
        }

        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
        logger.lifecycle("║ Detailed list of skipped tasks:                          ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")

        skippedTasks.sorted().forEach { taskName ->
          val truncatedName =
              if (taskName.length > 57) {
                taskName.take(54) + "..."
              } else {
                taskName.padEnd(57)
              }
          logger.lifecycle("║ • $truncatedName ║")
        }

        logger.lifecycle("╚═══════════════════════════════════════════════════════════╝")
        logger.lifecycle("")
      } else {
        logger.lifecycle(
            "⚡ FAST BUILD: Skipped ${skippedTasks.size} tasks (${taskCategories.entries.joinToString { "${it.value} ${it.key}" }})",
        )
        logger.lifecycle("   Run with -Pdebug.task.skip=true for detailed report")
      }
    }
  }
}
