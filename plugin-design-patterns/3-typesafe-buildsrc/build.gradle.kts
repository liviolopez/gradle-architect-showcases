plugins {
  application
  // Apply custom conventions from buildSrc
  id("kotlin-common-conventions")
  id("testing-conventions")
}

group = "com.example.typesafe"

version = "1.0.0"

application { mainClass.set("com.example.typesafe.TypeSafeAppKt") }

dependencies {
  // Kotlin standard library is added by kotlin-common-conventions

  // Additional dependencies using version catalog
  implementation(libs.kotlin.coroutines)

  // Testing dependencies are added by testing-conventions
}

tasks.test { useJUnitPlatform() }

// Custom task demonstrating type-safe access
tasks.register("showConfig") {
  doLast {
    println("Type-Safe BuildSrc Configuration")
    println("=================================")
    println("Project: ${project.name}")
    println("Version: ${project.version}")
    println("Group: ${project.group}")
    println("Kotlin Version: ${libs.versions.kotlin.get()}")
  }
}
