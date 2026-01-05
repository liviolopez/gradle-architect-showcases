import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.the

/** Documentation convention plugin - Dokka setup with source and javadoc jars */
class DocumentationConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      // Apply Dokka plugin
      pluginManager.apply("org.jetbrains.dokka")

      // Configure Dokka HTML task
      tasks.named("dokkaHtml", org.jetbrains.dokka.gradle.DokkaTask::class.java).configure {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))

        dokkaSourceSets.configureEach {
          includeNonPublic.set(false)
          skipDeprecated.set(false)
          skipEmptyPackages.set(true)
          reportUndocumented.set(true)
          platform.set(org.jetbrains.dokka.Platform.jvm)

          // Source links
          sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                providers
                    .gradleProperty("documentation.remote.url")
                    .map { java.net.URL(it) }
                    .orNull,
            )
            remoteLineSuffix.set("#L")
          }
        }
      }

      // Create sources jar
      val sourcesJar =
          tasks.register("sourcesJar", Jar::class.java) {
            archiveClassifier.set("sources")
            from(
                project
                    .the<org.gradle.api.plugins.JavaPluginExtension>()
                    .sourceSets
                    .getByName("main")
                    .allSource,
            )
          }

      // Create javadoc jar from dokka
      val dokkaJavadocJar =
          tasks.register("dokkaJavadocJar", Jar::class.java) {
            dependsOn("dokkaHtml")
            archiveClassifier.set("javadoc")
            from(tasks.named("dokkaHtml").get().outputs)
          }

      // Add to artifacts
      artifacts {
        add("archives", sourcesJar)
        add("archives", dokkaJavadocJar)
      }

      // Configurable documentation generation
      val documentationEnabled =
          findProperty("documentation.enabled")?.toString()?.toBoolean() ?: true
      if (!documentationEnabled) {
        tasks
            .matching { it.name.contains("dokka", ignoreCase = true) }
            .configureEach { enabled = false }
      }
    }
  }
}
