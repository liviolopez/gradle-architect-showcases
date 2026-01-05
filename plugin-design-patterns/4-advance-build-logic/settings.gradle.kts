rootProject.name = "enterprise-build-logic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

dependencyResolutionManagement {
  // Enforce centralized repository management
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }

}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }


// Include modules
include("modules:one")
include("modules:two")
