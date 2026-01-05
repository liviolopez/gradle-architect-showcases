# Root Convention Plugin with DSL - Usage Examples

## Overview

The `root-convention` plugin provides a powerful DSL for selectively applying conventions to subprojects. It's designed
to be applied **ONLY** to the root project.

## Basic Setup

### 1. Include build-logic in settings.gradle.kts

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "my-awesome-project"

include(":core")
include(":api")
include(":domain")
include(":spring-app")
include(":android-app")
include(":legacy-module")
```

### 2. Apply root-convention in root build.gradle.kts

```kotlin
// Root build.gradle.kts
plugins {
    id("root-convention")
}

gradlePatterns {
    // Configuration goes here
}
```

## Usage Patterns

### Pattern 1: Apply to All Subprojects

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    // Apply these conventions to ALL subprojects
    applyToAll(
        "kotlin-convention",
        "test-convention",
        "optimization-convention",
        "dependencies-convention"
    )
}
```

**Result:** Every subproject gets the 4 conventions applied automatically.

---

### Pattern 2: Apply to All Except Specific Projects

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    // Apply to all subprojects
    applyToAll(
        "kotlin-convention",
        "test-convention",
        "optimization-convention"
    )

    // But exclude these projects
    exclude(":legacy-module", ":experimental")
}
```

**Result:** All subprojects get conventions EXCEPT `:legacy-module` and `:experimental`.

---

### Pattern 3: Include Only Specific Projects

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    // Define default conventions
    applyToAll(
        "kotlin-convention",
        "test-convention"
    )

    // ONLY apply to these projects (ignore all others)
    include(":core", ":api", ":domain")
}
```

**Result:** ONLY `:core`, `:api`, and `:domain` get the conventions. All other projects are skipped.

---

### Pattern 4: Project-Specific Convention Overrides

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    // Default conventions for most projects
    applyToAll(
        "kotlin-convention",
        "test-convention",
        "optimization-convention"
    )

    // Spring app gets additional conventions
    forProject(":spring-app",
        "kotlin-convention",
        "spring-convention",
        "dependencies-convention",
        "documentation-convention"
    )

    // Android app gets different conventions
    forProject(":android-app",
        "kotlin-convention",
        "android-convention", // Custom convention (not shown here)
        "test-convention"
    )

    // Legacy module gets minimal conventions
    forProject(":legacy-module",
        "test-convention"
    )
}
```

**Result:**

- Most projects: kotlin + test + optimization
- `:spring-app`: kotlin + spring + dependencies + documentation
- `:android-app`: kotlin + android + test
- `:legacy-module`: test only

---

### Pattern 5: Complex Configuration

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    // Define base conventions
    applyToAll(
        "kotlin-convention",
        "test-convention",
        "optimization-convention",
        "dependencies-convention"
    )

    // Exclude legacy and experimental
    exclude(":legacy-module", ":experimental")

    // Spring Boot apps get special treatment
    forProject(":user-service",
        "kotlin-convention",
        "spring-convention",
        "documentation-convention"
    )

    forProject(":order-service",
        "kotlin-convention",
        "spring-convention",
        "documentation-convention"
    )

    // Library modules get documentation
    forProject(":common-lib",
        "kotlin-convention",
        "test-convention",
        "documentation-convention"
    )
}
```

---

## Configuration Properties

Control convention behavior via `gradle.properties`:

```properties
# Kotlin conventions
warnings.as.errors=false
lint.enabled=false

# Test conventions
test.showOutput=true
test.retry=true
test.ignoreFailures=false

# Optimization conventions
configuration.cache=true
debug.task.skip=false

# Dependencies conventions
use.serialization=true
use.logging=true
use.assertj=true
use.mockk=false

# Documentation conventions
documentation.enabled=true
documentation.remote.url=https://github.com/user/repo/tree/main

# Spring conventions
spring.devtools.enabled=true
spring.profiles.active=dev
```

---

## Build Output Examples

### Successful Application

```
> Task :help

╔═══════════════════════════════════════════════════════════╗
║         GRADLE PATTERNS - CONFIGURATION SUMMARY          ║
╠═══════════════════════════════════════════════════════════╣
║ Default Conventions: kotlin-convention, test-convention  ║
║ Mode: EXCLUDE specific                                   ║
║ Excluded Projects: 2                                     ║
║ Project-Specific Overrides: 3                            ║
╚═══════════════════════════════════════════════════════════╝

✅ Applying conventions to :core: kotlin-convention, test-convention
✅ Applying conventions to :api: kotlin-convention, test-convention
✅ Applying conventions to :spring-app: kotlin-convention, spring-convention, documentation-convention
⏭️  Skipping conventions for :legacy-module (excluded)
```

### With FAST_BUILD Mode

```bash
export FAST_BUILD=true
./gradlew build
```

```
╔═══════════════════════════════════════════════════════════╗
║         GRADLE PATTERNS - CONFIGURATION SUMMARY          ║
╠═══════════════════════════════════════════════════════════╣
║ Default Conventions: kotlin-convention, test-convention  ║
║ Mode: APPLY TO ALL                                       ║
╚═══════════════════════════════════════════════════════════╝

✅ Applying conventions to :core: kotlin-convention, test-convention
✅ Applying conventions to :api: kotlin-convention, test-convention

⚡ FAST BUILD: Skipped 15 tasks
   Run with -Pdebug.task.skip=true for detailed report
```

---

## Subproject build.gradle.kts Files

With the root convention plugin, subproject build files become MINIMAL:

```kotlin
// subprojects/core/build.gradle.kts
// Conventions already applied from root!

dependencies {
    // Only project-specific dependencies
    implementation(project(":common"))
}
```

```kotlin
// subprojects/spring-app/build.gradle.kts
// Conventions already applied from root!

dependencies {
    implementation(project(":core"))
    implementation(project(":api"))

    // Spring-specific dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

application {
    mainClass = "com.example.MainKt"
}
```

---

## Advanced: Conditional Application

```kotlin
plugins {
    id("root-convention")
}

gradlePatterns {
    applyToAll(
        "kotlin-convention",
        "test-convention"
    )

    // Apply spring convention only to projects ending with "-service"
    subprojects.forEach { subproject ->
        if (subproject.name.endsWith("-service")) {
            forProject(subproject.path,
                "kotlin-convention",
                "spring-convention",
                "test-convention"
            )
        }
    }
}
```

---

## Error Handling

If a convention fails to apply:

```
❌ Failed to apply convention 'invalid-convention' to :core:
   Plugin with id 'invalid-convention' not found.
```

The build will fail immediately with a clear error message.

---

## Benefits

### Before (manual application)

```kotlin
// Repeated in EVERY subproject
plugins {
    id("kotlin-convention")
    id("test-convention")
    id("optimization-convention")
}
```

### After (root convention)

```kotlin
// In root build.gradle.kts ONCE
gradlePatterns {
    applyToAll("kotlin-convention", "test-convention", "optimization-convention")
}

// Subproject build.gradle.kts
// Nothing! Conventions auto-applied!
```

### Result

- ✅ **DRY**: Configure once, apply everywhere
- ✅ **Flexible**: Include/exclude specific projects
- ✅ **Visible**: Clear summary of what's applied where
- ✅ **Maintainable**: Change conventions in one place
- ✅ **Scalable**: Works for 2 modules or 200 modules

---

## Complete Example Project

```
my-awesome-project/
├── build.gradle.kts          (root with root-convention)
├── settings.gradle.kts       (includes build-logic)
├── gradle.properties         (configuration)
│
├── build-logic/
│   └── (all convention plugins)
│
└── subprojects/
    ├── core/
    │   └── build.gradle.kts  (minimal, just dependencies)
    ├── api/
    │   └── build.gradle.kts  (minimal)
    ├── spring-app/
    │   └── build.gradle.kts  (minimal + app config)
    └── legacy-module/
        └── build.gradle.kts  (excluded from conventions)
```

This structure scales from small monorepos to enterprise-level projects with hundreds of modules!