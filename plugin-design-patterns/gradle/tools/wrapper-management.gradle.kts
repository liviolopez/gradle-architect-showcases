/**
 * Wrapper Management Tool
 *
 * Centralizes Gradle Wrapper across composite builds.
 *
 * Usage:
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperSync help
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperClean help
 */

/**
 * Auto-detects composite builds by finding directories with settings.gradle.kts
 * Excludes the root project and hidden directories
 */
fun findCompositeBuilds(rootDir: File): List<String> =
    rootDir.listFiles()
        ?.filter { it.isDirectory }
        ?.filter { !it.name.startsWith(".") }
        ?.filter {
          it.resolve("settings.gradle.kts").exists() || it.resolve("settings.gradle").exists()
        }
        ?.map { it.name }
        ?.sorted()
      ?: emptyList()

val wrapperFiles = listOf(
    "gradlew",
    "gradlew.bat",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties",
)

fun syncWrapperToComposites(rootDir: File) {
  val composites = findCompositeBuilds(rootDir)
  println("\n🔄 Syncing wrapper to composite builds...")
  composites.forEach { buildPath ->
    val targetDir = rootDir.resolve(buildPath)
    targetDir.resolve("gradle/wrapper").mkdirs()
    wrapperFiles.forEach { relativePath ->
      val source = rootDir.resolve(relativePath)
      val target = targetDir.resolve(relativePath)
      if (source.exists()) {
        source.copyTo(target, overwrite = true)
        println("  ✓ $buildPath/$relativePath")
      }
    }
    targetDir.resolve("gradlew").setExecutable(true)
  }
  println("✅ Wrapper synced to ${composites.size} composite builds\n")
}

fun cleanDuplicateWrappers(rootDir: File) {
  val composites = findCompositeBuilds(rootDir)
  println("\n🧹 Cleaning duplicate wrappers from composite builds...")
  composites.forEach { buildPath ->
    val targetDir = rootDir.resolve(buildPath)
    wrapperFiles.forEach { relativePath ->
      val file = targetDir.resolve(relativePath)
      if (file.exists()) {
        file.delete()
        println("  ✗ $buildPath/$relativePath")
      }
    }
    // Cleanup empty directories
    listOf("gradle/wrapper", "gradle").forEach { dir ->
      val d = targetDir.resolve(dir)
      if (d.exists() && d.isDirectory && d.listFiles()?.isEmpty() == true) {
        d.delete()
      }
    }
  }
  println("✅ Duplicate wrappers removed from ${composites.size} composite builds\n")
}

gradle.settingsEvaluated {
  val rootDir = rootProject.projectDir
  val wrapperSync = gradle.startParameter.projectProperties["wrapperSync"]
  val wrapperClean = gradle.startParameter.projectProperties["wrapperClean"]

  when {
    wrapperSync != null -> syncWrapperToComposites(rootDir)
    wrapperClean != null -> cleanDuplicateWrappers(rootDir)
  }
}
