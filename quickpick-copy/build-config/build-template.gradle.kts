import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

// ===============================================================================
// KOTLIN CONFIGURATION
// ===============================================================================

kotlin {
  jvmToolchain {
    vendor = JvmVendorSpec.JETBRAINS
    languageVersion = JavaLanguageVersion.of(21)
  }
}

// Separate task configuration for better organization
tasks {
  compileKotlin { compilerOptions.jvmTarget = JvmTarget.JVM_21 }

  compileTestKotlin { compilerOptions.jvmTarget = JvmTarget.JVM_21 }
}

// ===============================================================================
// SHARED CONFIGURATION FOR ALL PROJECTS
// ===============================================================================

allprojects {
  // Disable lint temporarily (consider using a property instead)
  val lintEnabled = findProperty("lint.enabled")?.toString()?.toBoolean() ?: false
  if (!lintEnabled) {
    tasks.matching { it.name.startsWith("lint") }.configureEach { enabled = false }
  }

  // -------------------------------------------------------------------------------
  // Kotlin Compilation Configuration
  // -------------------------------------------------------------------------------

  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
      apiVersion = KotlinVersion.KOTLIN_2_2
      languageVersion = KotlinVersion.KOTLIN_2_2

      // Warning configuration
      allWarningsAsErrors =
          providers.gradleProperty("warnings.as.errors").map { it.toBoolean() }.orElse(false)

      // Progressive mode enables new language features
      progressiveMode = true

      // Opt-in APIs (type-safe approach)
      optIn.addAll(
          "kotlin.RequiresOptIn",
          "kotlin.ExperimentalStdlibApi", // For experimental stdlib features
          "kotlinx.coroutines.ExperimentalCoroutinesApi", // For experimental coroutines
          "kotlinx.serialization.ExperimentalSerializationApi" // For experimental serialization
      )

      // Compiler arguments
      freeCompilerArgs
          .addAll(
              // Context receivers
              "-Xcontext-receivers",

              // Strict null-safety for Java interop
              "-Xjsr305=strict",

              // Skip version checks
              "-Xskip-prerelease-check",
              "-Xsuppress-version-warnings",

              // Performance optimizations
              "-Xbackend-threads=0", // Use all available cores
              "-Xir-optimizations-after-inlining", // Additional IR optimizations

              // Better type inference
              "-Xenable-builder-inference",

              // Report performance metrics (useful for CI)
              if (providers.environmentVariable("CI").isPresent) "-Xmetrics" else ""
          )
          .filter { it.isNotBlank() }
    }
  }

  // -------------------------------------------------------------------------------
  // Java Compilation Configuration
  // -------------------------------------------------------------------------------

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()

    options.apply {
      encoding = "UTF-8"
      isFork = true
      isIncremental = true
      isDeprecation = true

      // Additional optimizations
      compilerArgs.addAll(
          listOf(
              "--enable-preview", // Enable Java preview features
              "-Xlint:all,-processing", // Enable all warnings except annotation processing
              "-parameters" // Preserve parameter names for reflection
          )
      )

      // Memory configuration for forked compiler
      forkOptions.apply {
        memoryInitialSize = "256m"
        memoryMaximumSize = "1g"
        jvmArgs = listOf("-XX:+UseG1GC") // Use G1 garbage collector
      }
    }
  }

  // -------------------------------------------------------------------------------
  // Test Configuration
  // -------------------------------------------------------------------------------

  tasks.withType<Test>().configureEach {
    useJUnitPlatform {
      includeEngines("junit-jupiter")

      // Add system properties for tests
      systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
      systemProperty("junit.jupiter.execution.parallel.enabled", "true")
      systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    }

    // Parallel execution
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    // Test behavior
    ignoreFailures =
        providers.gradleProperty("test.ignoreFailures").map { it.toBoolean() }.orElse(false).get()

    // Memory configuration
    minHeapSize = "512m"
    maxHeapSize = "2g"
    jvmArgs("-XX:+UseG1GC", "-XX:MaxMetaspaceSize=512m")

    // Character encoding
    defaultCharacterEncoding = "UTF-8"

    // Test reports
    reports {
      html.required = true
      junitXml.required = true
    }

    // Enhanced test logging
    testLogging {
      events =
          setOf(
              TestLogEvent.STARTED,
              TestLogEvent.FAILED,
              TestLogEvent.SKIPPED,
              TestLogEvent.STANDARD_OUT,
              TestLogEvent.STANDARD_ERROR // Also capture error output
          )

      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
      showStandardStreams =
          providers.gradleProperty("test.showOutput").map { it.toBoolean() }.orElse(false).get()

      // Different configuration for CI
      if (providers.environmentVariable("CI").isPresent) {
        showStandardStreams = false // Less verbose in CI
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
      }
    }

    // Retry failed tests
    if (providers.gradleProperty("test.retry").isPresent) {
      retry {
        maxRetries = 2
        maxFailures = 5
      }
    }

    // Coverage integration (uncomment when ready)
    // finalizedBy(tasks.named("koverHtmlReport"))
  }

  // -------------------------------------------------------------------------------
  // Common Dependencies Configuration
  // -------------------------------------------------------------------------------

  configurations.all {
    // Force resolution strategies
    resolutionStrategy {
      // Fail eagerly on version conflict
      failOnVersionConflict()

      // Force specific versions if needed
      force("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")

      // Cache dynamic versions for 10 minutes
      cacheDynamicVersionsFor(10, TimeUnit.MINUTES)

      // Cache changing modules for 10 minutes
      cacheChangingModulesFor(10, TimeUnit.MINUTES)
    }

    // Exclude common conflicting dependencies
    exclude(group = "com.google.code.findbugs", module = "jsr305")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
  }
}

// ===============================================================================
// GRADLE CONFIGURATION
// ===============================================================================

// Build cache configuration
gradle.startParameter.apply {
  isBuildCacheEnabled = true
  isConfigurationCacheEnabled =
      providers.gradleProperty("configuration.cache").map { it.toBoolean() }.orElse(false).get()
}

// Task graph optimization
gradle.taskGraph.whenReady {
  val isDebugMode =
      providers
          .gradleProperty("debug.task.skip")
          .map { it.toBoolean() }
          .orElse(providers.environmentVariable("DEBUG_BUILD").map { it.toBoolean() })
          .getOrElse(false)

  if (providers.environmentVariable("FAST_BUILD").isPresent) {
    val skippedTasks = mutableListOf<String>()
    val taskCategories = mutableMapOf<String, Int>()

    allTasks.forEach { task ->
      val shouldSkip =
          when {
            task.name.contains("javadoc", ignoreCase = true) -> {
              taskCategories["JavaDoc"] = taskCategories.getOrDefault("JavaDoc", 0) + 1
              true
            }

            task.name.contains("sources", ignoreCase = true) -> {
              taskCategories["Sources"] = taskCategories.getOrDefault("Sources", 0) + 1
              true
            }

            task.name.contains("dokka", ignoreCase = true) -> {
              taskCategories["Documentation"] = taskCategories.getOrDefault("Documentation", 0) + 1
              true
            }

            task.name.contains("lint", ignoreCase = true) -> {
              taskCategories["Linting"] = taskCategories.getOrDefault("Linting", 0) + 1
              true
            }

            task.name.contains("ktlint", ignoreCase = true) -> {
              taskCategories["KtLint"] = taskCategories.getOrDefault("KtLint", 0) + 1
              true
            }

            else -> false
          }

      if (shouldSkip) {
        task.enabled = false
        skippedTasks.add("${task.project.name}:${task.name}")
      }
    }

    // Report skipped tasks
    if (skippedTasks.isNotEmpty()) {
      if (isDebugMode) {
        logger.lifecycle("╔═══════════════════════════════════════════════════════════╗")
        logger.lifecycle("║           FAST BUILD MODE - TASK SKIP REPORT             ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
        logger.lifecycle("║ Total tasks skipped: ${skippedTasks.size.toString().padEnd(36)} ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")

        taskCategories.forEach { (category, count) ->
          val line = "║ $category: ${count.toString().padEnd(48 - category.length)} ║"
          logger.lifecycle(line)
        }

        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
        logger.lifecycle("║ Detailed list of skipped tasks:                          ║")
        logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")

        skippedTasks.sorted().forEach { taskName ->
          val truncatedName =
              if (taskName.length > 57) {
                taskName.take(54) + "..."
              } else {
                taskName.padEnd(57)
              }
          logger.lifecycle("║ • $truncatedName ║")
        }

        logger.lifecycle("╚═══════════════════════════════════════════════════════════╝")
        logger.lifecycle("")
      } else {
        logger.lifecycle(
            "⚡ FAST BUILD: Skipped ${skippedTasks.size} tasks (${taskCategories.entries.joinToString { "${it.value} ${it.key}" }})"
        )
        logger.lifecycle("   Run with -Pdebug.task.skip=true for detailed report")
      }
    }
  }
}
