plugins { `kotlin-dsl` }

repositories {
  mavenCentral()
  gradlePluginPortal()
}

// Version constants - keep in sync with gradle/libs.versions.toml
object Versions {
  const val kotlin = "2.3.0"
  const val junit = "5.10.0"
  const val junitPlatform = "1.10.0"
}

dependencies {
  // Gradle plugins need to be referenced with full coordinates
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
  implementation("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")

  // For testing conventions
  implementation("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
  implementation("org.junit.platform:junit-platform-launcher:${Versions.junitPlatform}")
}
