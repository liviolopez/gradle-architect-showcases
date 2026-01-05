package com.example.typesafe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeSafeAppTest {

  private val app = TypeSafeApp()

  @Test
  fun `should validate build configuration successfully`() {
    val result = app.validateBuildConfig()

    assertTrue(result.isValid)
    assertEquals(1, result.warnings.size)
    assertEquals("Unused dependency detected", result.warnings[0])
    assertTrue(result.errors.isEmpty())
  }

  @Test
  fun `should generate optimization report`() {
    val report = app.performBuildOptimization()

    assertEquals(15, report.tasksOptimized)
    assertEquals("2.3 seconds", report.timeSaved)
    assertEquals(12, report.cacheHits)
    assertNotNull(report.executionTime)
  }

  @Test
  fun `should have valid config validator`() {
    val validator = ConfigValidator()

    assertTrue(validator.checkConfiguration())
    assertFalse(validator.getWarnings().isEmpty())
    assertTrue(validator.getErrors().isEmpty())
  }
}
