/*
 * Gradle Architect Showcases - Root Settings
 *
 * This settings file includes all plugin design pattern examples as separate builds.
 * Each pattern can be built and tested independently while being part of the showcase.
 *
 * Tools (see gradle/tools/):
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperSync help
 *   ./gradlew --init-script gradle/tools/wrapper-management.gradle.kts -PwrapperClean help
 *   ./gradlew --init-script gradle/tools/version-catalog-enforcement.gradle.kts <task>
 */

rootProject.name = "plugin-design-patterns"

// Composite builds - each pattern is a standalone, buildable example
includeBuild("1-enhanced-config")
includeBuild("2-modular-conventions")
includeBuild("3-typesafe-buildsrc")
includeBuild("4-advance-build-logic")
