/**
 * Repository configuration for settings.gradle.kts Usage in settings.gradle.kts: apply(from =
 * "$rootDir/gradle/conventions/repositories-settings.gradle.kts")
 */
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.PREFER_PROJECT

  repositories {
    // Primary repositories with content filtering
    mavenCentral {
      content {
        // Explicitly exclude Android artifacts from Maven Central
        excludeGroupByRegex("com\\.android.*")
        excludeGroupByRegex("androidx\\..*")
      }
    }

    google {
      content {
        // Google repository is specifically for Android/AndroidX
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("androidx\\..*")
        includeGroup("com.google.android.material")
      }
    }

    gradlePluginPortal {
      content {
        // Gradle plugins only
        includeGroupByRegex("org\\.gradle.*")
        includeGroupByRegex("com\\.gradle.*")
      }
    }

    // JetBrains repositories (conditional)
    if (providers.gradleProperty("use.jetbrains.repos").isPresent) {
      maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/") {
        name = "Kotlin Dev"
        content { includeGroup("org.jetbrains.kotlin") }
      }

      maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
        name = "Compose Dev"
        content { includeGroupByRegex("org\\.jetbrains\\.compose.*") }
      }
    }

    // Snapshots (conditional)
    if (providers.gradleProperty("use.snapshots").isPresent) {
      maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "Sonatype Snapshots"
        mavenContent { snapshotsOnly() }
      }
    }

    // Local repository
    val localRepoDir = rootProject.projectDir.resolve("gradle/maven-standalone")
    if (localRepoDir.exists()) {
      maven {
        name = "maven-standalone"
        url = localRepoDir.toURI()
        content { includeGroup("local.artifacts") }
      }
    }
  }
}
