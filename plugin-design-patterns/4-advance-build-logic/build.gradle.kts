plugins {
  // Apply convention plugins (apply false = available but not applied to root)
  id("enterprise.kotlin-conventions") apply false
  id("enterprise.testing-conventions") apply false
}

group = "com.example.enterprise"

version = "1.0.0"

// Root project configuration
tasks.register("clean", Delete::class) { delete(rootProject.layout.buildDirectory) }

// Aggregate task for running all tests
tasks.register("testAll") {
  dependsOn(subprojects.mapNotNull { it.tasks.findByName("test") })
  description = "Runs all tests in all modules"
  group = "verification"
}

// Task to display project structure
tasks.register("projectInfo") {
  doLast {
    println("Enterprise Build-Logic Project Structure:")
    println("==========================================")
    subprojects.forEach { project ->
      println("  📦 ${project.name}")
      println("     Path: ${project.path}")
      println("     Build Dir: ${project.layout.buildDirectory.get()}")
    }
  }
}
