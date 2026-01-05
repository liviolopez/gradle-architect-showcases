plugins {
  id("enterprise.kotlin-conventions")
  id("enterprise.testing-conventions")
  application
}

dependencies {
  implementation(libs.kotlin.coroutines)

  // Inter-module dependencies (when needed)
  // implementation(project(":modules:core"))
}

application { mainClass.set("com.example.enterprise.module.one.ModuleOneKt") }

// Module-specific configuration
tasks.named<JavaExec>("run") { jvmArgs = listOf("-Xmx512m") }
