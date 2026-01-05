plugins { java }

dependencies {
  // Note: Keep versions in sync with gradle/libs.versions.toml
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("io.mockk:mockk:1.13.8")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showStandardStreams = false
  }

  // Performance tuning
  maxHeapSize = "1G"
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}
