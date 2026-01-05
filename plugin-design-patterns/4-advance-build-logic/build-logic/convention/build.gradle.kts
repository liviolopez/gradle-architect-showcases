plugins { `kotlin-dsl` }

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.serialization.plugin)

  // Spring Boot plugin (for SpringConventionPlugin)
  implementation(libs.springboot.gradle.plugin)

  // Dokka plugin (for DocumentationConventionPlugin)
  implementation(libs.dokka.gradle.plugin)

  // For testing plugins
  testImplementation(libs.junit.jupiter.engine)
}

// Register all convention plugins
gradlePlugin {
  plugins {
    create("kotlinConvention") {
      id = "enterprise.kotlin-conventions"
      implementationClass = "KotlinConventionPlugin"
    }
    create("testConvention") {
      id = "enterprise.testing-conventions"
      implementationClass = "EnterpriseTestingPlugin"
    }
    create("optimizationConvention") {
      id = "optimization-convention"
      implementationClass = "OptimizationConventionPlugin"
    }
    create("dependenciesConvention") {
      id = "dependencies-convention"
      implementationClass = "DependenciesConventionPlugin"
    }
    create("documentationConvention") {
      id = "documentation-convention"
      implementationClass = "DocumentationConventionPlugin"
    }
    create("springConvention") {
      id = "spring-convention"
      implementationClass = "SpringConventionPlugin"
    }
    create("rootConvention") {
      id = "root-convention"
      implementationClass = "RootConventionPlugin"
    }
  }
}
