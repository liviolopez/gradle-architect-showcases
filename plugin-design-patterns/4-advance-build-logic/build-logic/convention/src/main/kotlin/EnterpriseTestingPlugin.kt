import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Testing convention plugin for enterprise modules. Standardizes testing configuration and
 * dependencies.
 */
class EnterpriseTestingPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      // Apply Java plugin for test configuration
      pluginManager.apply("java")

      // Configure test dependencies
      dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.0")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:5.10.0")
        "testImplementation"("org.assertj:assertj-core:3.24.2")
        "testImplementation"("io.mockk:mockk:1.13.8")
        "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
      }

      // Configure test task
      tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
          events =
              setOf(
                  TestLogEvent.PASSED,
                  TestLogEvent.SKIPPED,
                  TestLogEvent.FAILED,
                  TestLogEvent.STANDARD_OUT,
                  TestLogEvent.STANDARD_ERROR,
              )
          exceptionFormat = TestExceptionFormat.FULL
          showStandardStreams = false
          showCauses = true
          showStackTraces = true
        }

        // Performance and parallelization
        maxHeapSize = "2G"
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

        // Fail fast configuration
        failFast = findProperty("test.failFast")?.toString()?.toBoolean() ?: false

        // Test reports
        reports {
          html.required.set(true)
          junitXml.required.set(true)
        }
      }

      // Add test summary task
      tasks.register("testSummary") {
        group = "verification"
        description = "Displays test execution summary"
        dependsOn(tasks.withType<Test>())

        doLast {
          tasks.withType<Test>().forEach { testTask ->
            val results = testTask.reports.junitXml.outputLocation.get().asFile
            if (results.exists()) {
              println("Test Results for ${testTask.name}:")
              println("  Output: ${results.absolutePath}")
            }
          }
        }
      }
    }
  }
}
