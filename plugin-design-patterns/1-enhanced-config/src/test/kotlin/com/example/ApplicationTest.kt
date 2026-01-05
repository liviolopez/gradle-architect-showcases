package com.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplicationTest {

  private val app = Application()

  @Test
  fun `should greet with correct message`() {
    val result = app.greet("Test User")
    assertEquals("Hello, Test User! Welcome to Enhanced Config Pattern.", result)
  }

  @Test
  fun `should calculate sum correctly`() {
    val numbers = listOf(1, 2, 3, 4, 5)
    val result = app.calculateSum(numbers)
    assertEquals(15, result)
  }

  @Test
  fun `should handle empty list`() {
    val result = app.calculateSum(emptyList())
    assertEquals(0, result)
  }
}
