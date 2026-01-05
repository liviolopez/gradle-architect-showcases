import org.gradle.api.Plugin
import org.gradle.api.Project

/** Spring Boot convention plugin - Spring Boot configuration */
class SpringConventionPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {
      // Apply plugins
      pluginManager.apply("org.springframework.boot")
      pluginManager.apply("io.spring.dependency-management")
      pluginManager.apply("org.jetbrains.kotlin.plugin.spring")

      // Configure Spring Boot extension
      extensions.configure(org.springframework.boot.gradle.dsl.SpringBootExtension::class.java) {
        buildInfo()
      }

      // Add Spring dependencies
      dependencies.apply {
        // Spring Boot starters
        add("implementation", "org.springframework.boot:spring-boot-starter")

        // Kotlin support
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")

        // DevTools (development only)
        if (providers
              .gradleProperty("spring.devtools.enabled")
              .map { it.toBoolean() }
              .getOrElse(true)) {
          add("developmentOnly", "org.springframework.boot:spring-boot-devtools")
        }

        // Configuration processor
        add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")

        // Testing
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
      }

      // Configure BootJar task
      tasks
          .withType(org.springframework.boot.gradle.tasks.bundling.BootJar::class.java)
          .configureEach {
            archiveFileName.set("${project.name}.jar")

            // Enable layered jars for Docker
            layered { enabled.set(true) }
          }

      // Configure BootRun task
      tasks.withType(org.springframework.boot.gradle.tasks.run.BootRun::class.java).configureEach {
        jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-Dspring.output.ansi.enabled=ALWAYS")

        systemProperty(
            "spring.profiles.active",
            providers.gradleProperty("spring.profiles.active").getOrElse("dev"),
        )
      }
    }
  }
}
