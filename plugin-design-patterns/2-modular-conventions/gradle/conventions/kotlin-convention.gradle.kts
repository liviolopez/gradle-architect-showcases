import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

<*>

/**
 * Kotlin convention configuration
 * Apply with: apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")
 */

plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(21)
}

// Kotlin compilation settings
tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    apiVersion = KotlinVersion.KOTLIN_2_2
    languageVersion = KotlinVersion.KOTLIN_2_2

    // Warning configuration from properties
    allWarningsAsErrors = providers.gradleProperty("warnings.as.errors")
        .map { it.toBoolean() }
        .orElse(false)

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

// Lint configuration
val lintEnabled = findProperty("lint.enabled")?.toString()?.toBoolean() ?: false
if (!lintEnabled) {
  tasks.matching { it.name.startsWith("lint") }.configureEach {
    enabled = false
  }
}
