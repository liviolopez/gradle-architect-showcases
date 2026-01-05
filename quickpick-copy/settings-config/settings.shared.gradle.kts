// ----------------------- Shared Settings ------------------------
// Plugins and Utils for kts configurations, tasks and compilations

// Shoul be included in all Gradle projects of Caper, by adding the following
// line in the settings.gradle.kts:
//
// apply(from = "-- RELATIVE_PATH --/shared.settings.gradle.kts")

this practically is similar to init .. gradle . kts

// ================================================================

    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// val defaultPluginsAlias = listOf(
//    // Kotlin plugins
//    "kotlin.compose.compiler",
//    //"kotlin.android",
//    "kotlin.jvm",
//    "kotlin.multiplatform",
//    "kotlin.serialization",
//
//    // Android plugins
//    "android.application",
//    "android.baseline",
//    "android.library",
//    "android.test",
//
//    // Test and Quality plugins
//    "gmazzo.coverage",
//    "gmazzo.results",
//
//    // Gradle plugins
//    "gradle.cache.fix"
// )
//
// gradle.rootProject {
//    afterEvaluate {
//        val libs = the<VersionCatalogsExtension>().named("libs")
//        // Apply plugins dynamically after the project has been evaluated
//        defaultPluginsAlias.forEach { pluginAlias ->
//            plugins.apply(libs.findPlugin(pluginAlias).get().get().pluginId).apply(false)
//        }
//    }
// }
