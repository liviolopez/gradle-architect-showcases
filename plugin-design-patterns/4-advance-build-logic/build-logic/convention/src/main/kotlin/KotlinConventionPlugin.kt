import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Convention plugin for Kotlin configuration. Applies standard Kotlin settings across all modules.
 */
class KotlinConventionPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      pluginManager.apply("org.jetbrains.kotlin.jvm")

      extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
      }

      // Kotlin compilation configuration
      tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
          apiVersion = KotlinVersion.KOTLIN_2_2
          languageVersion = KotlinVersion.KOTLIN_2_2

          // Warning configuration from properties
          allWarningsAsErrors =
              providers.gradleProperty("warnings.as.errors").map { it.toBoolean() }.orElse(false)

          // Progressive mode for new language features
          progressiveMode = true

          // Type-safe opt-ins
          optIn.addAll(
              "kotlin.RequiresOptIn",
              "kotlin.ExperimentalStdlibApi",
              "kotlinx.coroutines.ExperimentalCoroutinesApi",
              "kotlinx.serialization.ExperimentalSerializationApi",
          )

          // Compiler arguments
          freeCompilerArgs.addAll(
              listOfNotNull(
                  "-Xcontext-receivers",
                  "-Xjsr305=strict",
                  "-Xskip-prerelease-check",
                  "-Xsuppress-version-warnings",
                  "-Xbackend-threads=0",
                  "-Xir-optimizations-after-inlining",
                  "-Xenable-builder-inference",
                  if (providers.environmentVariable("CI").isPresent) "-Xmetrics" else null,
              ),
          )
        }
      }

      // Apply lint configuration
      configureLint()
    }
  }

  private fun Project.configureLint() {
    val lintEnabled = findProperty("lint.enabled")?.toString()?.toBoolean() ?: false
    if (!lintEnabled) {
      tasks.matching { it.name.startsWith("lint") }.configureEach { enabled = false }
    }
  }
}
