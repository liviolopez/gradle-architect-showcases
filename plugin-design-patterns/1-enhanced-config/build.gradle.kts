plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}

group = "com.example.enhanced"

version = "1.0.0"

application { mainClass.set("com.example.ApplicationKt") }

dependencies {
  // Using version catalog bundles
  implementation(libs.bundles.kotlin.core)
  implementation(libs.kotlin.coroutines)

  // Testing dependencies from catalog
  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.junit.platform)
}

kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }

// Custom configuration extension
val enhancedConfig by
configurations.creating { description = "Enhanced configuration for special dependencies" }

// Example of enhanced build configuration
tasks.register("showConfig") {
  group = "help"
  description = "Shows the enhanced configuration"

  doLast {
    println("Enhanced Config Example")
    println("=======================")
    println("Project: ${project.name}")
    println("Version: ${project.version}")
    println("Kotlin Version: ${libs.versions.kotlin.get()}")
    println("JUnit Version: ${libs.versions.junit.get()}")
  }
}
