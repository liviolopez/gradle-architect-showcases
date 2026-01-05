// Root build.gradle.kts - Example with root-convention plugin

plugins {
  id("root-convention") // ← The magic plugin!
}

// Configure which conventions apply to which subprojects
gradlePatterns {
  // Default conventions for ALL subprojects (unless overridden)
  applyToAll(
      "kotlin-convention", "test-convention", "optimization-convention", "dependencies-convention",
  )

  // Exclude legacy modules that have their own configuration
  exclude(":legacy-module", ":experimental")

  // Spring Boot applications get special conventions
  forProject(
      ":user-service",
      "kotlin-convention",
      "spring-convention",
      "test-convention",
      "dependencies-convention",
      "documentation-convention",
  )

  forProject(
      ":order-service",
      "kotlin-convention",
      "spring-convention",
      "test-convention",
      "dependencies-convention",
      "documentation-convention",
  )

  // Library modules need documentation
  forProject(
      ":common-lib",
      "kotlin-convention",
      "test-convention",
      "dependencies-convention",
      "documentation-convention",
  )
}

// That's it! All subprojects are configured automatically!

// Optional: Root project tasks
tasks.register("printConventionSummary") {
  doLast {
    println("╔═══════════════════════════════════════════════╗")
    println("║     GRADLE PATTERNS - ACTIVE CONVENTIONS     ║")
    println("╚═══════════════════════════════════════════════╝")

    subprojects.forEach { sub ->
      println("${sub.path}: ${sub.plugins.map { it::class.simpleName }}")
    }
  }
}
