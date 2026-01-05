/**
 * Spring Boot convention Apply with: apply(from =
 * "$rootDir/gradle/conventions/spring-convention.gradle.kts")
 */
plugins {
  id("org.springframework.boot") version "3.2.0"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("plugin.spring") version "2.3.0"
}

springBoot {
  // Build info for actuator
  buildInfo()
}

dependencies {
  // Spring Boot starters (common ones)
  implementation("org.springframework.boot:spring-boot-starter")

  // Kotlin support
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // Spring Boot DevTools (development only)
  if (providers.gradleProperty("spring.devtools.enabled").map { it.toBoolean() }.getOrElse(true)) {
    developmentOnly("org.springframework.boot:spring-boot-devtools")
  }

  // Configuration processor
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  // Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
  // Configure boot jar
  archiveFileName.set("${project.name}.jar")

  // Layer tools for Docker
  layered { enabled = true }
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
  // JVM arguments for boot run
  jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-Dspring.output.ansi.enabled=ALWAYS")

  // System properties from gradle.properties
  systemProperty(
      "spring.profiles.active", providers.gradleProperty("spring.profiles.active").getOrElse("dev"),
  )
}
