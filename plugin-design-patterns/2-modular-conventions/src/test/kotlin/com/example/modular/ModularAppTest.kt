package com.example.modular

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModularAppTest {

  private val app = ModularApp()

  @Test
  fun `should initialize with correct configuration`() {
    val result = app.initialize()
    assertTrue(result.contains("Modular Conventions App"))
    assertTrue(result.contains("Version: 1.0.0"))
    assertTrue(result.contains("Environment: development"))
  }

  @Test
  fun `should process data and count occurrences`() {
    val data = listOf("kotlin", "java", "kotlin", "gradle")
    val result = app.processData(data)

    assertEquals(2, result["kotlin"])
    assertEquals(1, result["java"])
    assertEquals(1, result["gradle"])
  }

  @Test
  fun `should return sorted map`() {
    val data = listOf("zebra", "apple", "banana")
    val result = app.processData(data)

    val keys = result.keys.toList()
    assertEquals("apple", keys[0])
    assertEquals("banana", keys[1])
    assertEquals("zebra", keys[2])
  }
}
