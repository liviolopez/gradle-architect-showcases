package com.example.enterprise.module.two

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModuleTwoTest {

  private val module = ModuleTwo()

  @Test
  fun `should collect metrics successfully`() = runBlocking {
    val report = module.collectMetrics()

    assertNotNull(report.performance)
    assertNotNull(report.usage)
    assertNotNull(report.health)
    assertTrue(report.timestamp > 0)

    // Verify performance metrics
    assertTrue(report.performance.cpuUsage in 10.0..90.0)
    assertTrue(report.performance.memoryUsage in 20.0..80.0)
    assertTrue(report.performance.score in 60..100)

    // Verify usage metrics
    assertTrue(report.usage.activeUsers in 100..1000)
    assertTrue(report.usage.requestsPerSecond in 10..100)

    // Verify health metrics
    assertEquals(99.9, report.health.uptime)
    assertTrue(report.health.errorRate in 0.0..5.0)
  }

  @Test
  fun `should analyze metrics and provide recommendations`() = runBlocking {
    val report = module.collectMetrics()
    val analysis = module.analyzeMetrics(report)

    assertTrue(analysis.overallScore in 0..100)
    assertNotNull(analysis.recommendation)
    assertNotNull(analysis.alerts)

    // Verify recommendation logic
    when {
      analysis.overallScore >= 80 -> assertTrue(analysis.recommendation.contains("excellently"))
      analysis.overallScore >= 60 -> assertTrue(analysis.recommendation.contains("well"))
      else -> assertTrue(analysis.recommendation.contains("optimization"))
    }
  }

  @Test
  fun `should generate alerts for high resource usage`() {
    val highCpuReport =
        MetricsReport(
            performance = PerformanceMetrics(85.0, 50.0, 80),
            usage = UsageMetrics(500, 50, 85),
            health = HealthMetrics(99.9, 4.0, 90),
            timestamp = System.currentTimeMillis(),
        )

    val alerts = generateAlerts(highCpuReport)

    assertTrue(alerts.contains("High CPU usage detected"))
    assertTrue(alerts.contains("Error rate above threshold"))
  }

  @Test
  fun `should not generate alerts for normal usage`() {
    val normalReport =
        MetricsReport(
            performance = PerformanceMetrics(50.0, 40.0, 85),
            usage = UsageMetrics(300, 30, 80),
            health = HealthMetrics(99.9, 1.0, 95),
            timestamp = System.currentTimeMillis(),
        )

    val alerts = generateAlerts(normalReport)
    assertTrue(alerts.isEmpty())
  }
}
