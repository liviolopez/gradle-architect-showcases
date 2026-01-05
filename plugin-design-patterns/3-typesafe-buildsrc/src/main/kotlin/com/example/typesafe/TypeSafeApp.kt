package com.example.typesafe

import kotlin.time.measureTime

/**
 * Sample application demonstrating Type-Safe BuildSrc pattern. This pattern provides full IDE
 * support and compile-time validation.
 */
class TypeSafeApp {
  private val validator = ConfigValidator()

  fun validateBuildConfig(): BuildValidationResult {
    return BuildValidationResult(
        isValid = validator.checkConfiguration(),
        warnings = validator.getWarnings(),
        errors = validator.getErrors(),
    )
  }

  fun performBuildOptimization(): OptimizationReport {
    val duration = measureTime {
      // Simulate build optimization
      Thread.sleep(100)
    }

    return OptimizationReport(
        tasksOptimized = 15,
        timeSaved = "2.3 seconds",
        cacheHits = 12,
        executionTime = duration.toString(),
    )
  }
}

class ConfigValidator {
  fun checkConfiguration(): Boolean = true

  fun getWarnings(): List<String> = listOf("Unused dependency detected")

  fun getErrors(): List<String> = emptyList()
}

data class BuildValidationResult(
  val isValid: Boolean,
  val warnings: List<String>,
  val errors: List<String>
)

data class OptimizationReport(
  val tasksOptimized: Int,
  val timeSaved: String,
  val cacheHits: Int,
  val executionTime: String
)

fun main() {
  val app = TypeSafeApp()

  println("=== Type-Safe BuildSrc Pattern ===")
  println("Features: IDE support, compile-time checks, precompiled plugins\n")

  val validation = app.validateBuildConfig()
  println("Build Configuration:")
  println("  Valid: ${validation.isValid}")
  println("  Warnings: ${validation.warnings.joinToString()}")

  println("\nOptimization Report:")
  val report = app.performBuildOptimization()
  println("  Tasks optimized: ${report.tasksOptimized}")
  println("  Time saved: ${report.timeSaved}")
  println("  Cache hits: ${report.cacheHits}")
}
