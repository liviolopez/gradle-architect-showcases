/**
 * Version Catalog Enforcement Tool
 *
 * Validates that all projects follow the version catalog pattern
 * and don't hardcode versions in build files.
 *
 * Usage:
 *   ./gradlew --init-script gradle/tools/version-catalog-enforcement.gradle.kts <task>
 *
 * Or add to ~/.gradle/init.d/ for global enforcement
 */

val violationPatterns = listOf(
    // Direct version strings in dependencies
    """implementation\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in implementation",
    """api\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in api",
    """compileOnly\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in compileOnly",
    """runtimeOnly\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in runtimeOnly",
    """testImplementation\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in testImplementation",
    """kapt\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in kapt",
    """ksp\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in ksp",
    """annotationProcessor\s*\(\s*["'][^"']+:[^"']+:\d+""" to "Hardcoded version in annotationProcessor",

    // Android SDK versions hardcoded
    """compileSdk\s*=\s*\d+""" to "Hardcoded compileSdk - use libs.versions",
    """minSdk\s*=\s*\d+""" to "Hardcoded minSdk - use libs.versions",
    """targetSdk\s*=\s*\d+""" to "Hardcoded targetSdk - use libs.versions",
    """compileSdkVersion\s*\(\s*\d+""" to "Hardcoded compileSdkVersion - use libs.versions",
    """minSdkVersion\s*\(\s*\d+""" to "Hardcoded minSdkVersion - use libs.versions",
    """targetSdkVersion\s*\(\s*\d+""" to "Hardcoded targetSdkVersion - use libs.versions",

    // Version variables defined inline
    """val\s+\w*[Vv]ersion\s*=\s*["']\d+""" to "Version variable defined inline - use libs.versions",
    """const\s+val\s+\w*[Vv]ersion\s*=\s*["']\d+""" to "Version constant defined inline - use libs.versions",

    // Java/Kotlin toolchain hardcoded
    """jvmToolchain\s*\(\s*\d+\s*\)""" to "Hardcoded jvmToolchain - consider using libs.versions",

    // Plugin versions hardcoded (not using alias)
    """id\s*\(\s*["'][^"']+["']\s*\)\s*version\s*["']\d+""" to "Hardcoded plugin version - use alias(libs.plugins.xxx)",
)

val allowedPatterns = listOf(
    """libs\.versions\.""",
    """libs\.plugins\.""",
    """libs\.bundles\.""",
    """alias\s*\(\s*libs\.""",
    """version\.ref""",
    """// init-script-ignore""",
)

data class Violation(
  val file: File,
  val line: Int,
  val pattern: String,
  val message: String,
  val content: String
)

fun scanFile(file: File): List<Violation> {
  if (!file.exists() || !file.isFile) return emptyList()
  if (!file.name.endsWith(".gradle.kts") && !file.name.endsWith(".gradle")) return emptyList()

  val violations = mutableListOf<Violation>()

  file.readLines().forEachIndexed { index, line ->
    val isAllowed = allowedPatterns.any { pattern ->
      Regex(pattern).containsMatchIn(line)
    }
    if (isAllowed) return@forEachIndexed

    violationPatterns.forEach { (pattern, message) ->
      if (Regex(pattern).containsMatchIn(line)) {
        violations.add(
            Violation(
                file = file,
                line = index + 1,
                pattern = pattern,
                message = message,
                content = line.trim(),
            ),
        )
      }
    }
  }

  return violations
}

fun scanDirectory(dir: File): List<Violation> {
  val violations = mutableListOf<Violation>()

  dir.walkTopDown()
      .filter { it.isFile }
      .filter { it.name.endsWith(".gradle.kts") || it.name.endsWith(".gradle") }
      .filter { !it.path.contains(".gradle/") }
      .filter { !it.path.contains("/build/") }
      .forEach { file ->
        violations.addAll(scanFile(file))
      }

  return violations
}

fun printViolations(violations: List<Violation>, rootDir: File) {
  if (violations.isNotEmpty()) {
    println()
    println("╔══════════════════════════════════════════════════════════════════════╗")
    println("║           ⚠️  VERSION CATALOG ENFORCEMENT VIOLATIONS  ⚠️              ║")
    println("╠══════════════════════════════════════════════════════════════════════╣")
    println("║  All versions should be defined in gradle/libs.versions.toml        ║")
    println("║  Use libs.versions.xxx, libs.plugins.xxx, or libs.bundles.xxx       ║")
    println("╚══════════════════════════════════════════════════════════════════════╝")
    println()

    violations.groupBy { it.file }.forEach { (file, fileViolations) ->
      val relativePath = file.relativeTo(rootDir).path
      println("📄 $relativePath")
      println("─".repeat(72))

      fileViolations.forEach { violation ->
        println("  Line ${violation.line}: ${violation.message}")
        println("    └─ ${violation.content}")
      }
      println()
    }

    println("═".repeat(72))
    println("Total violations: ${violations.size}")
    println()
    println("To ignore a specific line, add comment: // init-script-ignore")
    println("═".repeat(72))
  } else {
    println()
    println("✅ Version Catalog Enforcement: All checks passed!")
    println()
  }
}

gradle.settingsEvaluated {
  val rootDir = rootProject.projectDir

  gradle.taskGraph.whenReady {
    val violations = scanDirectory(rootDir)
    printViolations(violations, rootDir)
  }
}
