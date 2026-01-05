package com.example.enterprise.module.one

/**
 * Module One - Part of Enterprise Build-Logic Pattern Demonstrates a module in a large-scale
 * project with independent build logic.
 */
class ModuleOne {
  private val service = CoreService()

  fun processBusinessLogic(input: BusinessRequest): BusinessResponse {
    val validated = service.validate(input)
    val processed = service.process(validated)

    return BusinessResponse(
        success = true, data = processed, timestamp = System.currentTimeMillis(),
    )
  }

  fun getModuleInfo(): ModuleInfo {
    return ModuleInfo(
        name = "Module One",
        version = "2.1.0",
        dependencies = listOf("core", "common", "api"),
        buildType = "enterprise-plugin",
    )
  }
}

class CoreService {
  fun validate(request: BusinessRequest): BusinessRequest {
    require(request.data.isNotEmpty()) { "Data cannot be empty" }
    return request
  }

  fun process(request: BusinessRequest): String {
    return "Processed: ${request.data.uppercase()}"
  }
}

data class BusinessRequest(val id: String, val data: String, val priority: Int = 0)

data class BusinessResponse(val success: Boolean, val data: String, val timestamp: Long)

data class ModuleInfo(
  val name: String,
  val version: String,
  val dependencies: List<String>,
  val buildType: String
)

fun main() {
  val module = ModuleOne()

  println("=== Enterprise Build-Logic Pattern ===")
  println("Module: ${module.getModuleInfo().name}")
  println("Build Type: ${module.getModuleInfo().buildType}")

  val request = BusinessRequest("REQ-001", "enterprise data", 1)
  val response = module.processBusinessLogic(request)

  println("\nProcessing Result:")
  println("  Success: ${response.success}")
  println("  Data: ${response.data}")
}
