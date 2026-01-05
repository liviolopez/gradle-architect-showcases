plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  application
}

group = "com.example.modular"

version = "1.0.0"

application { mainClass.set("com.example.modular.ModularAppKt") }

dependencies {
  // Using version catalog with bundles for better organization
  implementation(libs.bundles.kotlin.core)
  implementation(libs.bundles.kotlin.async)
  implementation(libs.kotlin.serialization.json)

  // Testing dependencies from catalog
  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.junit.platform)
}

kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }

// Modular convention configuration
interface ModularExtension {
  val moduleName: Property<String>
  val moduleType: Property<String>
}

val modular = extensions.create<ModularExtension>("modular")

modular.moduleName.convention(project.name)

modular.moduleType.convention("library")

// Custom task to display modular configuration
tasks.register("moduleInfo") {
  group = "help"
  description = "Shows module configuration"

  doLast {
    println("Modular Conventions Example")
    println("===========================")
    println("Module Name: ${modular.moduleName.get()}")
    println("Module Type: ${modular.moduleType.get()}")
    println("Kotlin Version: ${libs.versions.kotlin.get()}")
    println("Coroutines Version: ${libs.versions.coroutines.get()}")
  }
}
