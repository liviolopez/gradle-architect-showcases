package com.example.enterprise.module.two

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

/**
 * Module Two - Analytics Module Demonstrates another module in the Enterprise Build-Logic Pattern.
 * This module focuses on analytics and metrics collection.
 */
class ModuleTwo {
  private val metricsCollector = MetricsCollector()

  suspend fun collectMetrics(): MetricsReport {
    return coroutineScope {
      val performance = async { metricsCollector.collectPerformanceMetrics() }
      val usage = async { metricsCollector.collectUsageMetrics() }
      val health = async { metricsCollector.collectHealthMetrics() }

      MetricsReport(
          performance = performance.await(),
          usage = usage.await(),
          health = health.await(),
          timestamp = System.currentTimeMillis(),
      )
    }
  }

  fun analyzeMetrics(report: MetricsReport): AnalysisResult {
    val score = (report.performance.score + report.usage.score + report.health.score) / 3

    return AnalysisResult(
        overallScore = score,
        recommendation =
            when {
              score >= 80 -> "System performing excellently"
              score >= 60 -> "System performing well with room for improvement"
              else -> "System needs optimization"
            },
        alerts = generateAlerts(report),
    )
  }
}

class MetricsCollector {
  suspend fun collectPerformanceMetrics(): PerformanceMetrics {
    delay(100) // Simulate collection time
    return PerformanceMetrics(
        cpuUsage = Random.nextDouble(10.0, 90.0),
        memoryUsage = Random.nextDouble(20.0, 80.0),
        score = Random.nextInt(60, 100),
    )
  }

  suspend fun collectUsageMetrics(): UsageMetrics {
    delay(100)
    return UsageMetrics(
        activeUsers = Random.nextInt(100, 1000),
        requestsPerSecond = Random.nextInt(10, 100),
        score = Random.nextInt(70, 100),
    )
  }

  suspend fun collectHealthMetrics(): HealthMetrics {
    delay(100)
    return HealthMetrics(
        uptime = 99.9, errorRate = Random.nextDouble(0.0, 5.0), score = Random.nextInt(80, 100),
    )
  }
}

fun generateAlerts(report: MetricsReport): List<String> {
  val alerts = mutableListOf<String>()
  if (report.performance.cpuUsage > 80) alerts.add("High CPU usage detected")
  if (report.health.errorRate > 3) alerts.add("Error rate above threshold")
  return alerts
}

data class MetricsReport(
  val performance: PerformanceMetrics,
  val usage: UsageMetrics,
  val health: HealthMetrics,
  val timestamp: Long
)

data class PerformanceMetrics(val cpuUsage: Double, val memoryUsage: Double, val score: Int)

data class UsageMetrics(val activeUsers: Int, val requestsPerSecond: Int, val score: Int)

data class HealthMetrics(val uptime: Double, val errorRate: Double, val score: Int)

data class AnalysisResult(
  val overallScore: Int,
  val recommendation: String,
  val alerts: List<String>
)

fun main() = runBlocking {
  val module = ModuleTwo()

  println("=== Module Two: Analytics Service ===")
  println("Part of Enterprise Build-Logic Pattern\n")

  print("Collecting metrics...")
  val report = module.collectMetrics()
  println(" Done!")

  println("\nMetrics Summary:")
  println("  CPU Usage: ${"%.1f".format(report.performance.cpuUsage)}%")
  println("  Active Users: ${report.usage.activeUsers}")
  println("  Uptime: ${report.health.uptime}%")

  val analysis = module.analyzeMetrics(report)
  println("\nAnalysis:")
  println("  Overall Score: ${analysis.overallScore}/100")
  println("  Recommendation: ${analysis.recommendation}")
  if (analysis.alerts.isNotEmpty()) {
    println("  Alerts: ${analysis.alerts.joinToString(", ")}")
  }
}
