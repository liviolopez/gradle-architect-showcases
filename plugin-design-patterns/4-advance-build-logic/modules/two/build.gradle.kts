plugins {
  id("enterprise.kotlin-conventions")
  id("enterprise.testing-conventions")
  application
}

dependencies {
  implementation(libs.bundles.kotlin.core)
  implementation(libs.kotlin.coroutines)

  // Analytics-specific dependencies
  implementation(libs.kotlin.datetime)

  // Inter-module dependencies (when needed)
  // implementation(project(":modules:core"))

  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.junit.platform)
}

application { mainClass.set("com.example.enterprise.module.two.ModuleTwoKt") }

// Module-specific configuration for analytics
tasks.named<JavaExec>("run") { jvmArgs = listOf("-Xmx1G", "-XX:+UseG1GC") }
