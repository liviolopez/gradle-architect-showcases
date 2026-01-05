package com.example.modular

/**
 * Sample application using Modular Conventions pattern. Build configuration is split into focused,
 * reusable convention files.
 */
class ModularApp {
  private val config = AppConfiguration()

  fun initialize(): String {
    return """
            |=== Modular Conventions App ===
            |Version: ${config.version}
            |Environment: ${config.environment}
            |Conventions Applied: kotlin, test, optimization
        """
        .trimMargin()
  }

  fun processData(data: List<String>): Map<String, Int> {
    return data.groupingBy { it }.eachCount().toSortedMap()
  }
}

data class AppConfiguration(
  val version: String = "1.0.0",
  val environment: String = "development",
  val debug: Boolean = true
)

fun main() {
  val app = ModularApp()
  println(app.initialize())

  val data = listOf("kotlin", "java", "kotlin", "gradle", "kotlin", "java")
  println("\nLanguage frequency:")
  app.processData(data).forEach { (lang, count) -> println("  $lang: $count times") }
}
