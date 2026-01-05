# From 300 Lines of Pain to Maintainable Glory: Your Complete Gradle Modularization Guide

## 4 proven patterns to rescue your build configuration (pick the one that fits)

Before/after showing cluttered **347-line** `build.gradle.kts` transforming into 4 different organized patterns

---

## Table of Contents

- [Pattern 1: Modular Conventions](#pattern-1-modular-conventions-the-sweet-spot) — The Sweet Spot
- [Pattern 2: BuildSrc](#pattern-2-buildsrc-type-safe-conventions) — Type-Safe
- [Pattern 3: Build-Logic](#pattern-3-build-logic-enterprise-grade) — Enterprise
- [Pattern 4: Enhanced Templates](#pattern-4-enhanced-templates-quick-fix) — Quick Fix
- [Comparison Table](#the-comparison-table)
- [Decision Tree](#decision-tree-which-pattern-should-i-use)
- [Get All The Code](#get-all-the-code)

---

## The Day I Hit Rock Bottom

It was a Tuesday. Code review day. My teammate submitted a PR: "Add Redis caching dependency."

I opened `build.gradle.kts`.

**Line count: 347.**

I stared. My teammate asked: "Thoughts?"

I had thoughts. None were appropriate for Slack.

This file had:
- 15 plugins (3 were probably deprecated)
- 80+ dependencies (half duplicated across modules)
- 60 lines of Kotlin compiler flags (copied from various Stack Overflow posts)
- 40 lines of test configuration (nobody knew why it was there)
- Comments like: `// TODO: Clean this up (written 8 months ago)`
- And my personal favorite: `// Gradle made me cry` (I wrote this one)

**The worst part?** I was afraid to touch it.

Because the last time someone "cleaned up" this file, CI was red for 2 days and we had to revert while the CTO watched nervously.

---

## The Reality: How Build Files Die

It always starts innocently:

```kotlin
// build.gradle.kts - Day 1 ✨
plugins {
    kotlin("jvm") version "1.9.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

**15 lines. Clean. Beautiful.**

Then reality happens:

- Marketing wants faster builds → optimization config
- QA wants retry logic → test configuration sprawl
- Security wants scanning → more plugins
- DevOps wants caching → gradle.properties manipulation
- That senior engineer wants "proper compiler flags" → 30 more lines

Six months later, you have **The Monster**:

```kotlin
// build.gradle.kts - The Graveyard 💀

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jetbrains.dokka") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("org.sonarqube") version "4.4.0"
    id("jacoco")
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    // ... 5 more you forgot existed
}

kotlin {
    jvmToolchain(21)
    
    compilerOptions {
        // 50 lines of compiler configuration
        // Half copied from blog posts
        // Quarter from Stack Overflow
        // Rest from pure desperation
        
        optIn.addAll(
            "kotlin.RequiresOptIn",
            "kotlin.ExperimentalStdlibApi",
            "kotlin.time.ExperimentalTime",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
            "kotlinx.coroutines.FlowPreview",
            "kotlinx.serialization.ExperimentalSerializationApi",
            "kotlin.experimental.ExperimentalTypeInference",
            "kotlin.contracts.ExperimentalContracts"
            // ... you get the idea
        )
    }
}

dependencies {
    // 80 lines of dependency hell
}

tasks.withType<Test> {
    // 40 lines nobody understands
}

tasks.withType<KotlinCompile> {
    // 30 lines that duplicate the kotlin {} block
    // But removing them breaks things
}

// ... 150 more lines of pain
```

**347 lines of accumulated trauma.**

Sound familiar?

---

## The Solution: 4 Patterns (Pick Your Fighter)

Here's what I discovered: **There's no "one size fits all" solution.**

Small project? Large monorepo? Type safety obsessed? Each needs a different approach.

Let me show you **4 battle-tested patterns**, when to use each, and exactly how to migrate from your horrible file to each one.

---

## Understanding the Categories First

Before we dive into patterns, let's categorize what's in that 347-line monster.

Looking at any large build file, you'll find these **6 logical purposes**:

1. **Kotlin Configuration** (~60 lines)
   - JVM toolchain
   - Compiler options
   - Language features (opt-ins, progressive mode)
   - Compilation flags

2. **Test Configuration** (~40 lines)
   - JUnit setup
   - Parallel execution
   - Memory settings
   - Logging and reporting

3. **Optimization** (~35 lines)
   - Build cache
   - Configuration cache
   - FAST_BUILD mode
   - Task skipping

4. **Dependencies** (~80 lines)
   - Common dependencies
   - Version alignment
   - Platform BOMs
   - Test frameworks

5. **Documentation** (~30 lines)
   - Dokka configuration
   - JavaDoc settings
   - Source jars

6. **Framework-Specific** (~50 lines)
   - Spring Boot
   - Ktor setup
   - Android config
   - Whatever your stack uses

**Total:** ~295 lines of reusable configuration + ~50 lines of project-specific stuff

**The key insight:** Most of your build file is **reusable patterns**, not unique configuration.

---

## Pattern 1: Modular Conventions (The Sweet Spot)

### When to Use This

✅ **Perfect for:**
- Medium projects (4-10 modules)
- Teams that want simplicity
- Projects with varied module needs
- Open source (easy for contributors)

❌ **Skip if:**
- You have 1-3 modules (overkill)
- You NEED type safety (use BuildSrc instead)
- You're managing 50+ modules (use Build-Logic)

---

### The Migration: Horrible → Modular Conventions

📋 **[View complete example on GitHub](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/2-modular-conventions)**

**Step 1: Create the convention structure**

```bash
mkdir -p gradle/conventions
```

**Step 2: Extract by purpose**

Create 6 focused files:

```properties
gradle/conventions/
├── kotlin-convention.gradle.kts      # Kotlin config
├── test-convention.gradle.kts        # Testing
├── optimization-convention.gradle.kts # Performance
├── dependencies-convention.gradle.kts # Common deps
├── documentation-convention.gradle.kts # Dokka
└── spring-convention.gradle.kts      # Framework (if needed)
```

**Step 3: Move Kotlin configuration**

**From your horrible file:**
```kotlin
kotlin {
   jvmToolchain(21)
   compilerOptions {
      // ... 50 lines of pain
   }
}
```

**To: `gradle/conventions/kotlin-convention.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Standard Kotlin configuration for all modules
 */

plugins {
   kotlin("jvm")
}

kotlin {
   jvmToolchain(21)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
   compilerOptions {
      apiVersion = KotlinVersion.KOTLIN_2_2
      languageVersion = KotlinVersion.KOTLIN_2_2

      // Configurable via gradle.properties
      allWarningsAsErrors = providers.gradleProperty("warnings.as.errors")
         .map { it.toBoolean() }
         .orElse(false)

      progressiveMode = true

      // Standard opt-ins
      optIn.addAll(
         "kotlin.RequiresOptIn",
         "kotlin.ExperimentalStdlibApi",
         "kotlinx.coroutines.ExperimentalCoroutinesApi",
         "kotlinx.serialization.ExperimentalSerializationApi"
      )

      // Performance optimization flags
      freeCompilerArgs.addAll(
         listOfNotNull(
            "-Xcontext-receivers",
            "-Xjsr305=strict",
            "-Xbackend-threads=0",
            "-Xir-optimizations-after-inlining",
            "-Xenable-builder-inference",
            if (providers.environmentVariable("CI").isPresent) "-Xmetrics" else null
         )
      )
   }
}

// Configurable linting
val lintEnabled = findProperty("lint.enabled")?.toString()?.toBoolean() ?: false
if (!lintEnabled) {
   tasks.matching { it.name.startsWith("lint") }.configureEach {
      enabled = false
   }
}
```

**Step 4: Move Test configuration**

**To: `gradle/conventions/test-convention.gradle.kts`**

```kotlin
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

tasks.withType<Test>().configureEach {
   useJUnitPlatform {
      includeEngines("junit-jupiter")
      systemProperty("junit.jupiter.execution.parallel.enabled", "true")
      systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
   }

   // Smart parallelization
   maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

   minHeapSize = "512m"
   maxHeapSize = "2g"
   jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200")

   testLogging {
      events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED)
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true

      // Configurable output
      showStandardStreams = providers.gradleProperty("test.showOutput")
         .map { it.toBoolean() }
         .orElse(false)
         .get()
   }

   // Retry flaky tests
   if (providers.gradleProperty("test.retry").isPresent) {
      retry {
         maxRetries = 3
         maxFailures = 5
      }
   }
}
```

**Step 5: Move Optimization configuration**

**To: `gradle/conventions/optimization-convention.gradle.kts`**

```kotlin
// Build cache configuration
gradle.startParameter.apply {
   isBuildCacheEnabled = true
   isConfigurationCacheEnabled = providers.gradleProperty("configuration.cache")
      .map { it.toBoolean() }
      .orElse(false)
      .get()
}

// FAST_BUILD mode
gradle.taskGraph.whenReady {
   if (providers.environmentVariable("FAST_BUILD").isPresent) {
      val skippedTasks = mutableListOf<String>()

      allTasks.forEach { task ->
         val shouldSkip = when {
            task.name.contains("javadoc", ignoreCase = true) -> true
            task.name.contains("dokka", ignoreCase = true) -> true
            task.name.contains("lint", ignoreCase = true) -> true
            else -> false
         }

         if (shouldSkip) {
            task.enabled = false
            skippedTasks.add(task.name)
         }
      }

      if (skippedTasks.isNotEmpty()) {
         logger.lifecycle("⚡ FAST BUILD: Skipped ${skippedTasks.size} tasks")
      }
   }
}
```

**Step 6: Your new, clean build.gradle.kts**

```kotlin
// Apply conventions
apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")
apply(from = "$rootDir/gradle/conventions/test-convention.gradle.kts")
apply(from = "$rootDir/gradle/conventions/optimization-convention.gradle.kts")

plugins {
   id("org.springframework.boot") version "3.2.0"
}

dependencies {
   // ONLY project-specific dependencies
   implementation(project(":domain"))
   implementation(project(":application"))

   implementation("org.springframework.boot:spring-boot-starter-webflux")
}

application {
   mainClass = "com.example.MainKt"
}
```

**From 347 lines → 25 lines**

---

### Advantages: Modular Conventions

- ✅ **Modular and focused** - Each file does ONE thing
- ✅ **Pick and choose** - Use only what you need
- ✅ **Easy to understand** - <100 lines per file
- ✅ **Reusable** - Share across projects
- ✅ **No compilation overhead** - Changes apply immediately
- ✅ **Easy for contributors** - Small, readable files

### Disadvantages: Modular Conventions

- ❌ **No type safety** - Scripts aren't compiled
- ❌ **Limited IDE support** - No autocomplete inside conventions
- ❌ **Runtime errors only** - Typos only caught when tasks run

---

## Pattern 2: BuildSrc (Type-Safe Conventions)

### When to Use This

✅ **Perfect for:**
- Larger projects (10-50 modules)
- Teams that value type safety
- When IDE support matters
- Projects where refactoring is frequent

❌ **Skip if:**
- You have <10 modules (unnecessary overhead)
- Fast configuration time is critical
- Your team isn't comfortable with precompiled plugins

---

### The Migration: Horrible → BuildSrc

🚀 **[View complete example on GitHub](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/3-typesafe-buildsrc)**

**Step 1: Create BuildSrc structure**

```bash
mkdir -p buildSrc/src/main/kotlin
```

**Step 2: Create BuildSrc build file**

**Create: `buildSrc/build.gradle.kts`**

```kotlin
plugins {
   `kotlin-dsl`
}

repositories {
   mavenCentral()
   gradlePluginPortal()
}

dependencies {
   implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
}
```

**Step 3: Convert conventions to precompiled plugins**

**From your horrible file's Kotlin section:**
```kotlin
kotlin {
   jvmToolchain(21)
   // ... 50 lines
}
```

**To: `buildSrc/src/main/kotlin/kotlin-conventions.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
   kotlin("jvm")
}

kotlin {
   jvmToolchain(21)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
   compilerOptions {
      apiVersion = KotlinVersion.KOTLIN_2_2
      languageVersion = KotlinVersion.KOTLIN_2_2
      progressiveMode = true

      optIn.addAll(
         "kotlin.ExperimentalStdlibApi",
         "kotlinx.coroutines.ExperimentalCoroutinesApi"
      )

      freeCompilerArgs.addAll(
         "-Xbackend-threads=0",
         "-Xir-optimizations-after-inlining"
      )
   }
}
```

**Step 4: Create test convention**

**To: `buildSrc/src/main/kotlin/test-conventions.gradle.kts`**

```kotlin
tasks.withType<Test>().configureEach {
   useJUnitPlatform()
   maxParallelForks = Runtime.getRuntime().availableProcessors() / 2

   testLogging {
      events("passed", "skipped", "failed")
   }
}
```

**Step 5: Your new build.gradle.kts (TYPE-SAFE!)**

```kotlin
plugins {
   id("kotlin-conventions")      // ← IDE knows about this!
   id("test-conventions")        // ← Autocomplete works!
   id("org.springframework.boot") version "3.2.0"
}

dependencies {
   implementation(project(":domain"))
   implementation("org.springframework.boot:spring-boot-starter-webflux")
}
```

**Magic:** Your IDE now autocompletes `kotlin-conventions` and validates everything at compile time!

---

### Advantages: BuildSrc

- ✅ **Full type safety** - Catch errors at compile time
- ✅ **IDE autocomplete** - IntelliJ knows all your conventions
- ✅ **Precompiled plugins** - Faster after first build
- ✅ **Refactoring support** - Rename across entire build
- ✅ **Code navigation** - Jump to convention definitions

### Disadvantages: BuildSrc

- ❌ **Configuration time overhead** - BuildSrc compiles before every Gradle invocation
- ❌ **Changes require rebuild** - Edit convention = recompile BuildSrc
- ❌ **More complex** - Harder for beginners to understand
- ❌ **No independent versioning** - Tied to main project

---

## Pattern 3: Build-Logic (Enterprise-Grade)

### When to Use This

✅ **Perfect for:**
- Large monorepos (50+ modules)
- Enterprise organizations
- When you want to test build logic
- When you want independent versioning
- Multi-repo standardization

❌ **Skip if:**
- You have <50 modules (overkill)
- Your team is small (<10 engineers)
- You don't need independent convention versioning

---

### The Migration: Horrible → Build-Logic

🏗️ **[View complete example on GitHub](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/4-advance-build-logic)**

**Step 1: Create build-logic structure**

```bash
mkdir -p build-logic/convention/src/main/kotlin
```

**Step 2: Configure build-logic as included build**

**Create: `build-logic/settings.gradle.kts`**

```kotlin
dependencyResolutionManagement {
   repositories {
      mavenCentral()
      gradlePluginPortal()
   }
}

rootProject.name = "build-logic"

include("convention")
```

**Create: `build-logic/convention/build.gradle.kts`**

```kotlin
plugins {
   `kotlin-dsl`
}

dependencies {
   implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
}
```

**Step 3: Create convention plugins as REAL Kotlin classes**

**Create: `build-logic/convention/src/main/kotlin/KotlinConventionPlugin.kt`**

```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class KotlinConventionPlugin : Plugin<Project> {
   override fun apply(project: Project) {
      with(project) {
         pluginManager.apply("org.jetbrains.kotlin.jvm")

         extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(21)
         }

         tasks.withType(KotlinCompilationTask::class.java).configureEach {
            compilerOptions {
               progressiveMode.set(true)

               freeCompilerArgs.addAll(
                  "-Xbackend-threads=0",
                  "-opt-in=kotlin.ExperimentalStdlibApi"
               )
            }
         }
      }
   }
}
```

**Create: `build-logic/convention/src/main/kotlin/TestConventionPlugin.kt`**

```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class TestConventionPlugin : Plugin<Project> {
   override fun apply(project: Project) {
      with(project) {
         tasks.withType(Test::class.java).configureEach {
            useJUnitPlatform()
            maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
         }
      }
   }
}
```

**Step 4: Register plugins**

**Create: `build-logic/convention/src/main/kotlin/convention.gradle.kts`** (plugin descriptor)

```kotlin
gradlePlugin {
   plugins {
      register("kotlinConvention") {
         id = "kotlin-convention"
         implementationClass = "KotlinConventionPlugin"
      }
      register("testConvention") {
         id = "test-convention"
         implementationClass = "TestConventionPlugin"
      }
   }
}
```

**Step 5: Include build-logic in main project**

**In your root `settings.gradle.kts`:**

```kotlin
pluginManagement {
   includeBuild("build-logic")
}

rootProject.name = "my-awesome-project"
include(":domain", ":application", ":infrastructure")
```

**Step 6: Your new build.gradle.kts**

```kotlin
plugins {
   id("kotlin-convention")  // From build-logic!
   id("test-convention")
   id("org.springframework.boot") version "3.2.0"
}

dependencies {
   implementation(project(":domain"))
}
```

**BONUS: You can now test your build logic!**

**Create: `build-logic/convention/src/test/kotlin/KotlinConventionPluginTest.kt`**

```kotlin
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinConventionPluginTest {
   @Test
   fun `plugin applies kotlin jvm`() {
      val project = ProjectBuilder.builder().build()
      project.pluginManager.apply("kotlin-convention")

      assertTrue(project.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm"))
   }
}
```

---

### Advantages: Build-Logic

- ✅ **Maximum scalability** - Handles 100+ modules
- ✅ **Unit testable** - Test your build configuration!
- ✅ **Independent versioning** - Update conventions separately
- ✅ **Full type safety** - Real Kotlin code
- ✅ **Can be published** - Share across organization
- ✅ **IDE support** - Same as BuildSrc

### Disadvantages: Build-Logic

- ❌ **High initial complexity** - Steep learning curve
- ❌ **Overkill for small projects** - Unnecessary for <50 modules
- ❌ **More setup** - Requires separate build structure
- ❌ **Team training needed** - Not intuitive for beginners

---

## Pattern 4: Enhanced Templates (Quick Fix)

### When to Use This

✅ **Perfect for:**
- Small projects (1-3 modules)
- Prototypes and MVPs
- When you need results NOW
- Personal/side projects

❌ **Skip if:**
- You have 4+ modules (use Modular Conventions)
- You need to share config across projects
- Your build file is growing fast

---

### The Migration: Horrible → Enhanced Template

📝 **[View complete quickpick templates on GitHub](https://github.com/liviolopez/gradle-architect-showcases/tree/main/quickpick-copy)**

**Step 1: Copy the enhanced template**

📂 [View on GitHub](https://github.com/liviolopez/gradle-architect-showcases/tree/main/quickpick-copy/build-config/build-template.gradle.kts)

**Step 2: Replace your horrible file**

```kotlin
// improved-build-template.gradle.kts
plugins {
   kotlin("jvm") version "2.2.0"
}

// Configuration via properties - NO CODE CHANGES NEEDED
val lintEnabled = findProperty("lint.enabled")?.toString()?.toBoolean() ?: false
val warningsAsErrors = providers.gradleProperty("warnings.as.errors")
   .map { it.toBoolean() }
   .orElse(false)

kotlin {
   jvmToolchain(21)

   compilerOptions {
      allWarningsAsErrors = warningsAsErrors
      progressiveMode = true

      // Built-in optimizations
      freeCompilerArgs.addAll(
         "-Xbackend-threads=0",
         "-Xir-optimizations-after-inlining",
         "-Xenable-builder-inference"
      )

      optIn.addAll(
         "kotlin.ExperimentalStdlibApi",
         "kotlinx.coroutines.ExperimentalCoroutinesApi"
      )
   }
}

tasks.withType<Test>().configureEach {
   useJUnitPlatform()
   maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

// FAST_BUILD mode
if (providers.environmentVariable("FAST_BUILD").isPresent) {
   gradle.taskGraph.whenReady {
      allTasks.filter { it.name.contains("lint") }.forEach { it.enabled = false }
   }
}

dependencies {
   // Your dependencies here
}
```

**Step 3: Configure via gradle.properties**

```properties
lint.enabled=false
warnings.as.errors=false
test.showOutput=true
```

---

### Advantages: Enhanced Templates

- ✅ **Simple** - One file to copy
- ✅ **Fast setup** - 5 minutes
- ✅ **Property-based config** - No code changes
- ✅ **Built-in optimizations** - Better than default
- ✅ **FAST_BUILD mode** - Skip non-essential tasks

### Disadvantages: Enhanced Templates

- ❌ **Still large** - 200+ lines in one file
- ❌ **Not modular** - All-or-nothing approach
- ❌ **Hard to maintain** - Grows over time
- ❌ **Not reusable** - Copy-paste to share

---

## BONUS: Root-Only Plugin Application (Multi-Module Magic)

One common pattern: **Apply conventions in root project, affect all subprojects**.

### Method 1: Using allprojects

**In root `build.gradle.kts`:**

```kotlin
allprojects {
   apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")
}

// Or with BuildSrc/Build-Logic:
allprojects {
   apply(plugin = "kotlin-convention")
}
```

**Effect:** Every subproject gets the convention applied.

### Method 2: Using subprojects (excluding root)

**In root `build.gradle.kts`:**

```kotlin
subprojects {
   apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")
}

// Root project doesn't get the convention
```

### Method 3: Conditional Application

**In root `build.gradle.kts`:**

```kotlin
subprojects {
   // Only apply to modules with Kotlin code
   plugins.withId("org.jetbrains.kotlin.jvm") {
      apply(from = "$rootDir/gradle/conventions/kotlin-convention.gradle.kts")
   }
}
```

### Method 4: Root Convention Plugin (Build-Logic)

**Create: `build-logic/convention/src/main/kotlin/RootConventionPlugin.kt`**

```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class RootConventionPlugin : Plugin<Project> {
   override fun apply(project: Project) {
      require(project == project.rootProject) {
         "RootConventionPlugin must be applied to root project only"
      }

      project.subprojects {
         // Apply to all subprojects
         pluginManager.apply(KotlinConventionPlugin::class.java)
         pluginManager.apply(TestConventionPlugin::class.java)
      }
   }
}
```

**In root `build.gradle.kts`:**

```kotlin
plugins {
   id("root-convention")  // Applies conventions to ALL subprojects!
}
```

**Subproject build files become MINIMAL:**

```kotlin
// subproject/build.gradle.kts
// Conventions already applied from root!

dependencies {
   // Only project-specific dependencies
   implementation(project(":common"))
}
```

---

## The Comparison Table

<div style="font-size: 0.85em;">

| Aspect | Enhanced | Modular | BuildSrc | Build-Logic |
|--------|----------|---------|----------|-------------|
| **Setup** | 🟢 5min | 🟡 30min | 🟡 2hrs | 🔴 1day |
| **Complexity** | 🟢 Low | 🟡 Med | 🟡 Med | 🔴 High |
| **Type Safety** | ❌ | ❌ | ✅ | ✅ |
| **IDE Support** | ✅ | ⚠️ | ✅ | ✅ |
| **Modularity** | ❌ | ✅ | ✅ | ✅ |
| **Reusability** | ⚠️ | ✅ | ✅ | ✅ |
| **Config Time** | ✅ | ✅ | ⚠️ | ✅ |
| **Change Speed** | ✅ | ✅ | ❌ | ❌ |
| **Testability** | ❌ | ❌ | ⚠️ | ✅ |
| **Scale** | 1-3 | 4-10 | 10-50 | 50+ |
| **Learning** | 🟢 Easy | 🟡 Mod | 🟡 Mod | 🔴 Steep |
| **Best For** | Protos | Teams | Large | Enterprise |

</div>

---

## Decision Tree: Which Pattern Should I Use?

```
How many modules do you have?
│
├── 1-3 modules
│   └── ✅ Enhanced Templates
│
├── 4-10 modules
│   │
│   ├── Need type safety?
│   │   ├── Yes → ✅ BuildSrc
│   │   └── No → ✅ Modular Conventions
│   │
│
├── 10-50 modules
│   │
│   ├── Type safety critical?
│   │   ├── Yes → ✅ BuildSrc
│   │   └── No → ✅ Modular Conventions
│   │
│
└── 50+ modules
    │
    ├── Need versioning/testing?
    │   ├── Yes → ✅ Build-Logic
    │   └── No → ✅ BuildSrc
    │
```

---

## My Recommendations

### For Most Teams: Start with Modular Conventions

**Why:**
- Sweet spot of simplicity and power
- Easy to understand and maintain
- No compilation overhead
- Grows with your project

**Upgrade to BuildSrc when:**
- You hit 10+ modules
- Type safety becomes critical
- Refactoring is frequent

**Upgrade to Build-Logic when:**
- You hit 50+ modules
- You need independent versioning
- Multiple repos need to share conventions

### For Small Projects: Enhanced Templates

Don't overcomplicate. Use the template. Ship features.

### For Large Orgs: Build-Logic

The complexity pays off at scale. Invest in it early.

---

## Common Pitfalls (Learn from My Pain)

### Pitfall #1: Over-Engineering Too Early

**Don't do this:** "Let's set up Build-Logic for our 3-module project!"

**Do this:** Start simple, evolve as needed.

### Pitfall #2: Forgetting Configuration Properties

**Don't do this:** Hardcode everything in conventions

**Do this:** Make it configurable via `gradle.properties`

```properties
lint.enabled=false
warnings.as.errors=true
test.retry=true
```

### Pitfall #3: Mixing Patterns

**Don't do this:** Use Modular Conventions AND BuildSrc for different things

**Do this:** Pick ONE pattern and commit to it

### Pitfall #4: No Documentation

**Don't do this:** Extract conventions without explaining them

**Do this:** Add a README to `gradle/conventions/` explaining each file

---

## The Action Plan

### Week 1: Audit Your Current Build

1. Open `build.gradle.kts`
2. Count the lines
3. Categorize by purpose (Kotlin, Test, Optimization, etc.)
4. Identify what's reusable vs project-specific

### Week 2: Choose Your Pattern

Use the decision tree above. When in doubt, start with **Modular Conventions**.

### Week 3: Execute the Migration

Follow the migration guide for your chosen pattern.

Test after each extraction:
```bash
./gradlew clean build
```

### Week 4: Refine and Share

- Add `gradle.properties` configuration
- Document your conventions
- Share with your team
- Reuse across modules

---

## Conclusion: You Can Do This

Your 300-line build file isn't a badge of honor. It's technical debt.

Modern Gradle configuration should be:

- **Modular** (focused, single-purpose files)
- **Configurable** (via properties, not code)
- **Reusable** (DRY across modules)
- **Understandable** (clear purpose, well-documented)

Pick your pattern. Start migrating. Your future self will thank you.

---

## Get All The Code

All 4 patterns, copy-paste ready:

⭐ **[github.com/liviolopez/gradle-architect-showcases](https://github.com/liviolopez/gradle-architect-showcases)**

**Explore the patterns:**
- 📋 [Quick Copy-Paste Templates](https://github.com/liviolopez/gradle-architect-showcases/tree/main/quickpick-copy) - Instant setup
- 🔧 [Modular Conventions](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/2-modular-conventions) - The sweet spot
- 🚀 [Type-Safe BuildSrc](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/3-typesafe-buildsrc) - IDE support
- 🏗️ [Enterprise Build-Logic](https://github.com/liviolopez/gradle-architect-showcases/tree/main/plugin-design-patterns/4-advance-build-logic) - Maximum scalability

**Also includes:**
- ✅ Complete working examples for each pattern
- ✅ Migration guides
- ✅ Settings templates (Maven repos, version catalogs)
- ✅ Compile and test locally

---

## Let's Connect

Which pattern matches your project? Already using one? Share your experience in the comments!

*Originally published January 2026*

---

*P.S. - If you're still maintaining a 500+ line build.gradle.kts, this is your intervention. Pick a pattern. Start today.* 🚀
