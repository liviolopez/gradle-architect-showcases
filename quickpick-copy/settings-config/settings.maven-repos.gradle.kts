// ===================================================================
// Shared Maven Repository Configuration for Gradle Settings
// ===================================================================
//
// PURPOSE:
// Centralized repository configuration for consistent dependency resolution
// across multiple projects. This file should be included in your settings.gradle.kts
//
// USAGE:
// apply(from = "path/to/settings.maven-repos.gradle.kts")
//
// BENEFITS:
// - Centralized repository management
// - Optimized repository order for faster builds
// - Security controls (exclusive content, repository mode)
// - Consistent plugin and dependency resolution
//
// ===================================================================

// ===================================================================
// PLUGIN MANAGEMENT
// ===================================================================
//
// Configure repositories for Gradle plugins.
// Repository order matters: most frequently used repos should be first.
//
pluginManagement {
  repositories {
    // TIER 1: Standard repositories (fastest, most common)
    gradlePluginPortal()        // Official Gradle plugins
    google()                    // Android & Google plugins
    mavenCentral()              // Most OSS plugins

    // TIER 2: JetBrains ecosystem (Kotlin, Compose, IntelliJ)
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    maven("https://maven.pkg.jetbrains.space/public/p/gradle-plugins/maven")

    // TIER 3: Snapshots and alternative mirrors
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://plugins.gradle.org/m2")  // Alternative to gradlePluginPortal()

    // TIER 4: Local repositories (for offline builds or custom plugins)
    // USE CASE: Store custom or patched plugins locally
    mavenLocal {
      name = "maven-standalone"
      url = rootProject.projectDir.toPath().resolve("gradle/maven-standalone").toFile().toURI()
    }
  }

  // ===================================================================
  // PLUGIN VERSION MANAGEMENT
  // ===================================================================

  // Pin Kotlin version for consistency across all modules
  // USE CASE: Prevents version conflicts when subprojects don't specify versions
  plugins {
    kotlin("jvm") version "2.2.20"
  }

  // ===================================================================
  // PLUGIN RESOLUTION STRATEGIES
  // ===================================================================

  // USE CASE: Redirect plugin requests to enable gradual migration
  // EXAMPLE: Migrate from Anvil to Metro without updating all modules at once
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "xyz.plugin-fake.tool") {
        useModule("another.plugin-id.tool:another.plugin-id.tool.gradle.plugin:1.0.0")
      }
    }
  }
}

// ===================================================================
// SHARED VERSION CATALOG (OPTIONAL)
// ===================================================================
//
// USE CASE: Share dependency versions across multiple root projects
// EXAMPLE: Monorepo with multiple Gradle builds that need consistent versions
//
// USAGE:
// applySharedVersionCatalog("shared")
// Then access as: libs-shared.kotlinx.coroutines
//
fun applySharedVersionCatalog(name: String) {
  // Use provider for configuration cache compatibility
  val sharedVersionCatalogPath: String
  get() = providers
      .exec {
        // Command to get the shared version catalog path
        // CUSTOMIZE THIS: Replace with your actual path resolution logic
        commandLine("pwd")
      }
      .standardOutput
      .asText
      .get()
      .trim() + "/my-catalog-file.versions.toml"

  dependencyResolutionManagement.versionCatalogs
      .create("libs-$name")
      .from(files("$sharedVersionCatalogPath/gradle/libs.versions.toml"))
}

// ===================================================================
// SECURITY AND BEST PRACTICES
// ===================================================================

// RECOMMENDED: Fail if subprojects declare their own repositories
// USE CASE: Enforce centralized repository management for security and consistency
// UNCOMMENT TO ENABLE:
// dependencyResolutionManagement.repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

// DEBUG HELPER: Uncomment to verify which project is using this settings file
// println("Applied to root project: ${rootProject.projectDir.absolutePath}")

// ===================================================================
// DEPENDENCY RESOLUTION MANAGEMENT
// ===================================================================
//
// Configure repositories for library dependencies.
// Repository order matters: most frequently used repos should be first.
//
dependencyResolutionManagement.repositories {

  // ---------------------------------------------------------------------
  // TIER 1: Standard repositories (fastest, most common)
  // ---------------------------------------------------------------------
  mavenCentral()              // Most OSS libraries
  google()                    // Android, AndroidX, Google libraries

  // ---------------------------------------------------------------------
  // TIER 2: JetBrains ecosystem
  // ---------------------------------------------------------------------
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  maven("https://androidx.dev/storage/compose-compiler/repository")
  maven("https://maven.pkg.jetbrains.space/public/p/gradle-plugins/maven")

  // ---------------------------------------------------------------------
  // TIER 3: Specialized repositories
  // ---------------------------------------------------------------------

  // Machine Learning and Data Science (Kotlin DS, Let's Plot, DataNucleus)
  maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
  maven("https://www.datanucleus.org/downloads/maven2/")

  // IntelliJ Platform (for plugin development)
  maven("https://www.jetbrains.com/intellij-repository/releases")
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/releases")
  maven("https://cache-redirector.jetbrains.com/plugins.jetbrains.com/maven")

  // Kotlin Package Manager (KPM)
  maven("https://packages.jetbrains.team/maven/p/kpm/public/")

  // Mozilla libraries (GeckoView, etc.)
  maven("https://maven.mozilla.org/maven2/")

  // ---------------------------------------------------------------------
  // TIER 4: Snapshots and mirrors
  // ---------------------------------------------------------------------
  maven("https://www.jetbrains.com/intellij-repository/snapshots")
  maven("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/snapshots")
  maven("https://oss.sonatype.org/content/repositories/snapshots")

  // Legacy mirrors (consider removing if not actively used)
  maven("https://plugins.gradle.org/m2")
  maven("https://repo1.maven.org/maven2")
  maven("https://repo.maven.apache.org/maven2/")
  maven("https://dl.google.com/dl/android/maven2/")

  // ---------------------------------------------------------------------
  // LOCAL REPOSITORIES
  // ---------------------------------------------------------------------

  // Maven Standalone Repository
  // USE CASE: Store local copies of:
  // - Libraries no longer available in public repos
  // - Custom/patched libraries (e.g., Jetified AARs)
  // - Corporate internal libraries
  //
  // SETUP: Place AARs/JARs in gradle/maven-standalone following Maven structure:
  // gradle/maven-standalone/com/example/library/1.0.0/library-1.0.0.aar
  //
  // NOTE: Replace 'caperGradleSystemPath' with your actual path variable
  mavenLocal {
    name = "maven-standalone"
    url = File("$caperGradleSystemPath/gradle/maven-standalone").toURI()
  }

  // ---------------------------------------------------------------------
  // EXCLUSIVE CONTENT (SECURITY BEST PRACTICE)
  // ---------------------------------------------------------------------

  // Exclusive Content Pattern
  // USE CASE: Restrict specific libraries to specific repositories
  // BENEFIT: Prevents dependency confusion attacks and speeds up resolution

  // EXAMPLE 1: Esper Device SDK (only from Esper's repository)
  exclusiveContent {
    forRepositories(maven("https://artifact.esper.io/artifactory/esper-device-sdk/"))
    filter {
      includeVersionByRegex("io.esper.devicesdk", ".*", ".*")
    }
  }

  // EXAMPLE 2: JitPack (deprecated, use with caution)
  // USE CASE: Restrict JitPack to known libraries only
  // SECURITY: JitPack builds on-demand from GitHub, prefer Maven Central when available
  exclusiveContent {
    forRepositories(maven("https://jitpack.io"))
    filter {
      // PATTERN 1: Include entire group (all artifacts from this org)
      includeGroup("com.github.johnsmith")

      // PATTERN 2: Include specific module only
      includeModule("com.github.devtools", "usb-device-manager")

      // PATTERN 3: Include with version constraints (only 1.x versions)
      includeVersionByRegex("com.github.netutils", "NetworkMonitor", "1\\..*")

      // PATTERN 4: Include all artifacts matching group pattern
      includeGroupByRegex("io\\.github\\..*")

      // PATTERN 5: Include specific module with any version
      includeVersionByRegex("com.github.uikit", "CustomSwitch", ".*")

      // PATTERN 6: Include multiple artifacts from same group (alternative to includeGroup)
      includeModule("com.github.widgetlab", "ProgressAnimations")
      includeModule("com.github.widgetlab", "LoadingIndicators")

      // PATTERN 7: Include with specific version range (semantic versioning)
      includeVersionByRegex("com.github.toolbox", "UtilityKit", "[2-3]\\..*")

      // Add your JitPack libraries here following the same pattern
      includeVersionByRegex("com.github.yourcompany", ".*", ".*")
    }
  }

  // ---------------------------------------------------------------------
  // ADD MORE EXCLUSIVE CONTENT PATTERNS AS NEEDED
  // ---------------------------------------------------------------------

  // TEMPLATE: Copy and modify for your exclusive repositories
  exclusiveContent {
    forRepositories(maven("https://your-private-repo.com"))
    filter {
      includeGroup("com.yourcompany")
      // OR use regex:
      // includeVersionByRegex("com.yourcompany", ".*", ".*")
    }
  }
}
