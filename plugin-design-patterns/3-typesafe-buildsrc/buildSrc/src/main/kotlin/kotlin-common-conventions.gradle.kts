import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { kotlin("jvm") }

dependencies {
  // Note: Keep versions in sync with gradle/libs.versions.toml
  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.addAll("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
  }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
