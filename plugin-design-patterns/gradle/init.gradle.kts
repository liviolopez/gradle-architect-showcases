/**
 * Composite Build Tools - Init Script Loader
 *
 * This script loads modular tools from gradle/tools/:
 *   - wrapper-management.gradle.kts     → Sync/clean wrapper across composites
 *   - version-catalog-enforcement.gradle.kts → Validate no hardcoded versions
 *
 * Usage (all tools):
 *   ./gradlew --init-script gradle/init.gradle.kts <task>
 *
 * Usage (individual tools):
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperSync help
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperClean help
 *   ./gradlew --init-script gradle/tools/version-catalog-enforcement.gradle.kts <task>
 */

val toolsDir = File(initscript.sourceFile?.parentFile, "tools")

if (toolsDir.exists()) {
  toolsDir.listFiles()
      ?.filter { it.extension == "kts" }
      ?.sortedBy { it.name }
      ?.forEach { tool ->
        apply(from = tool)
      }
}
