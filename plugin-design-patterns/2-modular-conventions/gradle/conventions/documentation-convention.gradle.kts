/**
 * Documentation convention - Dokka configuration Apply with: apply(from =
 * "$rootDir/gradle/conventions/documentation-convention.gradle.kts")
 */
plugins { id("org.jetbrains.dokka") version "1.9.10" }

tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml").configure {
  outputDirectory.set(layout.buildDirectory.dir("dokka/html"))

  dokkaSourceSets {
    configureEach {
      // Include all source sets
      includeNonPublic.set(false)

      // Skip dependencies
      skipDeprecated.set(false)
      skipEmptyPackages.set(true)

      // Report undocumented
      reportUndocumented.set(true)

      // Platform configuration
      platform.set(org.jetbrains.dokka.Platform.jvm)

      // Source links
      sourceLink {
        localDirectory.set(file("src/main/kotlin"))
        remoteUrl.set(
            providers.gradleProperty("documentation.remote.url").map { java.net.URL(it) }.orNull,
        )
        remoteLineSuffix.set("#L")
      }
    }
  }
}

// Create sources jar
val sourcesJar by
tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

// Create javadoc jar from dokka
val dokkaJavadocJar by
tasks.registering(Jar::class) {
  dependsOn(tasks.dokkaHtml)
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml.flatMap { it.outputDirectory })
}

// Add to artifacts
artifacts {
  add("archives", sourcesJar)
  add("archives", dokkaJavadocJar)
}

// Configure documentation tasks
val documentationEnabled = findProperty("documentation.enabled")?.toString()?.toBoolean() ?: true

if (!documentationEnabled) {
  tasks.matching { it.name.contains("dokka", ignoreCase = true) }.configureEach { enabled = false }
}
