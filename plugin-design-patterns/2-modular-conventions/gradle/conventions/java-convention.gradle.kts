import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Java convention configuration Apply with: apply(from =
 * "$rootDir/gradle/conventions/java-convention.gradle.kts")
 */
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
            "-parameters", // Preserve parameter names for reflection
        ),
    )

    // Memory configuration for forked compiler
    forkOptions.apply {
      memoryInitialSize = "256m"
      memoryMaximumSize = "1g"
      jvmArgs = listOf("-XX:+UseG1GC")
    }
  }
}
