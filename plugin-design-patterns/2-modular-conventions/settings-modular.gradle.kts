/**
 * Simplified settings.gradle.kts using modular conventions This demonstrates how to use the
 * convention files
 */
@file:Suppress("UnstableApiUsage")

// ===============================================================================
// GRADLE FEATURES
// ===============================================================================

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// ===============================================================================
// PLUGIN MANAGEMENT
// ===============================================================================

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

// ===============================================================================
// REPOSITORY & DEPENDENCY MANAGEMENT
// ===============================================================================

// Apply modular repository configuration
apply(from = "$rootDir/gradle/conventions/repositories-settings.gradle.kts")

// ===============================================================================
// BUILD CACHE
// ===============================================================================

buildCache {
  local {
    isEnabled = true
    directory = file("${rootProject.projectDir}/.gradle/build-cache")
    removeUnusedEntriesAfterDays = 30
  }

  // Remote cache for CI
  if (providers.environmentVariable("CI").isPresent) {
    val cacheUrl = providers.environmentVariable("GRADLE_CACHE_URL")
    val cachePush = providers.environmentVariable("GRADLE_CACHE_PUSH")

    if (cacheUrl.isPresent) {
      remote<HttpBuildCache> {
        url = uri(cacheUrl.get())
        isPush = cachePush.map { it.toBoolean() }.getOrElse(false)
      }
    }
  }
}

// ===============================================================================
// PROJECT STRUCTURE
// ===============================================================================

rootProject.name =
    "kover-dynamic-coverage-plugin"

// For multi-module projects, include subprojects here
// include(":core")
// include(":app")
