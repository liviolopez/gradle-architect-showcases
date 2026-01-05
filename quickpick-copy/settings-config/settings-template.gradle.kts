// ===============================================================================
// FEATURE PREVIEWS
// ===============================================================================

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// ===============================================================================
// PLUGIN MANAGEMENT
// ===============================================================================

pluginManagement {
  // Define plugin versions centrally
  val kotlinVersion = "2.3.0"
  val foojayResolverVersion = "1.0.0"

  repositories {
    // Order matters - most likely sources first for performance
    gradlePluginPortal()
    mavenCentral()
    google()

    // JetBrains repositories (only if needed)
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/") {
      name = "Kotlin Dev"
      content { includeGroup("org.jetbrains.kotlin") }
    }

    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
      name = "Compose Dev"
      content { includeGroupByRegex("org\\.jetbrains\\.compose.*") }
    }

    // Snapshots (only if needed, usually slow)
    if (providers.gradleProperty("use.snapshots").isPresent) {
      maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "Sonatype Snapshots"
        mavenContent { snapshotsOnly() }
      }
    }

    // Local repository (last resort)
    mavenLocal()
  }

  plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.gradle.toolchains.foojay-resolver-convention") version foojayResolverVersion
  }

  // Resolution strategy for plugins
  resolutionStrategy {
    eachPlugin {
      // Custom plugin resolution rules if needed
      when (requested.id.id) {
        "com.android.application",
        "com.android.library" -> {
          useModule("com.android.tools.build:gradle:${requested.version}")
        }
      }
    }
  }
}

// ===============================================================================
// DEPENDENCY RESOLUTION MANAGEMENT
// ===============================================================================

dependencyResolutionManagement {
  // Enforce repository declaration mode
  repositoriesMode =
      RepositoriesMode.PREFER_PROJECT // or FAIL_ON_PROJECT_REPOS for stricter control

  repositories {
    // Primary repositories
    mavenCentral {
      content {
        // Optimize by excluding known groups not in Maven Central
        excludeGroupByRegex("com\\.android.*")
        excludeGroupByRegex("androidx.*")
      }
    }

    google {
      content {
        // Only Android/AndroidX artifacts from Google
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("androidx.*")
        includeGroup("com.google.android.material")
      }
    }

    // JetBrains repositories with content filtering
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
      name = "Compose Dev"
      content { includeGroupByRegex("org\\.jetbrains\\.compose.*") }
    }

    maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven") {
      name = "Kotlin Data Science"
      content { includeGroup("org.jetbrains.kotlinx") }
    }

    // IntelliJ repositories (only if building IDE plugins)
    if (providers.gradleProperty("build.intellij.plugin").isPresent) {
      maven("https://www.jetbrains.com/intellij-repository/releases") { name = "IntelliJ Releases" }
      maven("https://cache-redirector.jetbrains.com/intellij-dependencies") {
        name = "IntelliJ Dependencies"
      }
    }

    // Local/custom repository with proper isolation
    maven {
      name = "maven-standalone"
      url = rootProject.projectDir.resolve("gradle/maven-standalone").toURI()
      content { includeGroup("local.artifacts") }
    }
  }

  // Version catalogs configuration
  versionCatalogs {
    // Option 1: External catalog file
    if (file("../../gradle/libs.versions.toml").exists()) {
      create("libs") { from(files("../../gradle/libs.versions.toml")) }
    }

    // Option 2: Define inline for small projects
    create("libs") {
      version("kotlin", "2.3.0")
      version("junit", "5.10.1")
      version("coroutines", "1.8.0")

      library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
      library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
      library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core")
          .versionRef("coroutines")

      bundle("testing", listOf("junit-jupiter"))
    }
  }
}

// ===============================================================================
// BUILD CACHE CONFIGURATION
// ===============================================================================

buildCache {
  local {
    isEnabled = true
    directory = file("${System.getProperty("user.home")}/.gradle/build-cache")
    removeUnusedEntriesAfterDays = 30
  }

  // Remote cache configuration (if available)
  if (providers.environmentVariable("GRADLE_REMOTE_CACHE_URL").isPresent) {
    remote<HttpBuildCache> {
      url = uri(providers.environmentVariable("GRADLE_REMOTE_CACHE_URL").get())
      isEnabled = true
      isPush = providers.environmentVariable("CI").isPresent

      credentials {
        username = providers.environmentVariable("GRADLE_REMOTE_CACHE_USERNAME").orNull
        password = providers.environmentVariable("GRADLE_REMOTE_CACHE_PASSWORD").orNull
      }
    }
  }
}

// ===============================================================================
// PROJECT STRUCTURE
// ===============================================================================

rootProject.name = "connectivity-app"

// Include projects with validation
val projectsToInclude =
    listOf(
        ":connectivity",
        ":app",
        ":core",
        ":debugger:integration-test-app",
        ":debugger:monitor-analyzer-core",
        ":debugger:android-orchestration-test"
    )

projectsToInclude.forEach { projectPath ->
  include(projectPath)

  // Validate project directory exists
  val projectDir = rootDir.resolve(projectPath.replace(":", "/"))
  if (!projectDir.exists()) {
    logger.warn("⚠️ Project directory does not exist: $projectDir")
  }
}

// ===============================================================================
// GRADLE PROPERTIES
// ===============================================================================

// Apply custom initialization scripts
val initScriptDir = rootDir.resolve("gradle/init.d")

if (initScriptDir.exists() && initScriptDir.isDirectory) {
  initScriptDir
      .listFiles { file -> file.extension == "gradle" }
      ?.forEach { script -> apply(from = script) }
}

// ===============================================================================
// PERFORMANCE OPTIMIZATIONS
// ===============================================================================

gradle.startParameter.apply {
  // Parallel execution
  isParallelProjectExecutionEnabled = true

  // Configure on demand
  isConfigureOnDemand = true

  // Maximum workers
  maxWorkerCount = Runtime.getRuntime().availableProcessors()
}
