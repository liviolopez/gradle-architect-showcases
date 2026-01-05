package com.example.enterprise.module.one

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ModuleOneTest {

  private val module = ModuleOne()

  @Test
  fun `should process business logic successfully`() {
    val request = BusinessRequest("TEST-001", "test data", 1)
    val response = module.processBusinessLogic(request)

    assertTrue(response.success)
    assertEquals("Processed: TEST DATA", response.data)
    assertTrue(response.timestamp > 0)
  }

  @Test
  fun `should return correct module info`() {
    val info = module.getModuleInfo()

    assertEquals("Module One", info.name)
    assertEquals("2.1.0", info.version)
    assertEquals(listOf("core", "common", "api"), info.dependencies)
    assertEquals("enterprise-plugin", info.buildType)
  }

  @Test
  fun `should validate non-empty data`() {
    val service = CoreService()
    val emptyRequest = BusinessRequest("ID-001", "", 0)

    assertThrows<IllegalArgumentException> { service.validate(emptyRequest) }
  }

  @Test
  fun `should process request correctly`() {
    val service = CoreService()
    val request = BusinessRequest("ID-001", "hello world", 1)
    val validated = service.validate(request)
    val result = service.process(validated)

    assertEquals("Processed: HELLO WORLD", result)
  }
}
