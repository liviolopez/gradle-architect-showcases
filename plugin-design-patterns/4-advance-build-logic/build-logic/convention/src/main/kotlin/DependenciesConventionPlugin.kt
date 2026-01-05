import org.gradle.api.Plugin
import org.gradle.api.Project

/** Dependencies convention plugin - Common dependencies for all modules */
class DependenciesConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      dependencies.apply {
        // Kotlin standard library
        add("implementation", platform("org.jetbrains.kotlin:kotlin-bom:2.3.0"))

        // Coroutines
        val coroutinesVersion = findProperty("coroutines.version")?.toString() ?: "1.8.0"
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

        // Serialization (optional)
        val useSerialization = findProperty("use.serialization")?.toString()?.toBoolean() ?: false
        if (useSerialization) {
          val serializationVersion = findProperty("serialization.version")?.toString() ?: "1.6.2"
          add(
              "implementation",
              "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion",
          )
        }

        // Logging
        val useLogging = findProperty("use.logging")?.toString()?.toBoolean() ?: true
        if (useLogging) {
          add("implementation", "io.github.oshai:kotlin-logging-jvm:5.1.0")
          add("implementation", "ch.qos.logback:logback-classic:1.4.11")
        }

        // Testing
        add("testImplementation", platform("org.junit:junit-bom:5.10.1"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        add(
            "testImplementation",
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion",
        )

        // AssertJ (optional)
        val useAssertJ = findProperty("use.assertj")?.toString()?.toBoolean() ?: true
        if (useAssertJ) {
          add("testImplementation", "org.assertj:assertj-core:3.24.2")
        }

        // Mockk (optional)
        val useMockk = findProperty("use.mockk")?.toString()?.toBoolean() ?: false
        if (useMockk) {
          add("testImplementation", "io.mockk:mockk:1.13.8")
        }
      }

      // Dependency resolution strategy
      configurations.configureEach {
        resolutionStrategy {
          // Fail on version conflicts
          failOnVersionConflict()

          // Force specific versions
          force(
              "org.jetbrains.kotlin:kotlin-stdlib:2.3.0",
              "org.jetbrains.kotlin:kotlin-reflect:2.3.0",
          )

          // Cache strategies
          cacheDynamicVersionsFor(10, "minutes")
          cacheChangingModulesFor(4, "hours")
        }
      }
    }
  }
}
