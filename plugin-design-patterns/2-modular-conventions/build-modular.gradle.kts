/**
 * Simplified build.gradle.kts using modular conventions This demonstrates how to use the convention
 * files
 */
plugins {
  kotlin("jvm") version "2.3.0"
  kotlin("plugin.serialization") version "2.3.0"
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "1.3.0"
}

// Apply modular conventions
apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")

apply(from = "$rootDir/gradle/conventions/java-convention.gradle.kts")

apply(from = "$rootDir/gradle/conventions/test-convention.gradle.kts")

apply(from = "$rootDir/gradle/conventions/dependencies-convention.gradle.kts")

apply(from = "$rootDir/gradle/conventions/optimization-convention.gradle.kts")

group = "all4.dev"

version = "1.0.0"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.jgit)

  // Testing
  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.junit.platform.launcher)
}

gradlePlugin {
  website = "https://github.com/all4dev/kover-dynamic-coverage-plugin"
  vcsUrl = "https://github.com/all4dev/kover-dynamic-coverage-plugin.git"

  plugins {
    create("koverDynamicCoverage") {
      id = "all4.dev.kover.dynamic.coverage"
      displayName = "Kover Dynamic Coverage Plugin"
      description = "Advanced coverage reporting with baseline comparison"
      implementationClass = "all4.dev.coverage.KoverDynamicCoveragePlugin"
      tags = listOf("coverage", "kotlin", "testing", "code-quality")
    }
  }
}
