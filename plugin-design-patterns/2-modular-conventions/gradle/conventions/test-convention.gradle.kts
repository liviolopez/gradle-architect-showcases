import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/**
 * Test convention configuration Apply with: apply(from =
 * "$rootDir/gradle/conventions/test-convention.gradle.kts")
 */
tasks.withType<Test>().configureEach {
  useJUnitPlatform {
    includeEngines("junit-jupiter")

    // System properties for tests
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
  }

  // Parallel execution
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

  // Test behavior from properties
  ignoreFailures =
      providers.gradleProperty("test.ignoreFailures").map { it.toBoolean() }.orElse(false).get()

  // Memory configuration
  minHeapSize = "512m"
  maxHeapSize = "2g"
  jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200")

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
            TestLogEvent.STANDARD_ERROR,
        )

    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
    showStandardStreams =
        providers.gradleProperty("test.showOutput").map { it.toBoolean() }.orElse(false).get()

    // Different configuration for CI
    if (providers.environmentVariable("CI").isPresent) {
      showStandardStreams = false
      events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
  }

  // Retry failed tests
  if (providers.gradleProperty("test.retry").isPresent) {
    retry {
      maxRetries = 3
      maxFailures = 5
    }
  }
}
