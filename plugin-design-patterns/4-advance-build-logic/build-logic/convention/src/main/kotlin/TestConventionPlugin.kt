import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/** Test convention plugin - JUnit 5 configuration with parallel execution */
class TestConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform {
          includeEngines("junit-jupiter")

          // Parallel execution
          systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
          systemProperty("junit.jupiter.execution.parallel.enabled", "true")
          systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
        }

        // Smart parallelization
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

        // Memory configuration
        minHeapSize = "512m"
        maxHeapSize = "2g"
        jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200")

        // Character encoding
        defaultCharacterEncoding = "UTF-8"

        // Test reports
        reports {
          html.required.set(true)
          junitXml.required.set(true)
        }

        // Enhanced logging
        testLogging {
          events =
              setOf(
                  TestLogEvent.STARTED,
                  TestLogEvent.FAILED,
                  TestLogEvent.SKIPPED,
                  TestLogEvent.PASSED,
              )

          exceptionFormat = TestExceptionFormat.FULL
          showExceptions = true
          showCauses = true
          showStackTraces = true

          // Configurable output visibility
          showStandardStreams =
              providers.gradleProperty("test.showOutput").map { it.toBoolean() }.getOrElse(false)

          // CI-specific configuration
          if (providers.environmentVariable("CI").isPresent) {
            showStandardStreams = false
            events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
          }
        }

        // Note: Test retry requires gradle-test-retry plugin
        // Add to build-logic dependencies: implementation("org.gradle:test-retry-gradle-plugin:1.5.8")
        // if (providers.gradleProperty("test.retry").isPresent) {
        //   retry { maxRetries.set(3); maxFailures.set(5) }
        // }

        // Fail behavior
        ignoreFailures =
            providers.gradleProperty("test.ignoreFailures").map { it.toBoolean() }.getOrElse(false)
      }
    }
  }
}
