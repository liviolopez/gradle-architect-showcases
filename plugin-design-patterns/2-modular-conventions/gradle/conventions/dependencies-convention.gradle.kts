import java.util.concurrent.TimeUnit

/**
 * Dependencies and resolution strategy convention Apply with: apply(from =
 * "$rootDir/gradle/conventions/dependencies-convention.gradle.kts")
 */
configurations.all {
  // Force resolution strategies
  resolutionStrategy {
    // Fail on version conflict
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
