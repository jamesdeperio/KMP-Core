# 🛠 KMP Core & Debug Tools

This repository contains the foundational shared infrastructure and debugging tools for the Kotlin Multiplatform ecosystem. 

## 📦 Modules

- **`:shared:core`**: Core utilities, networking (Ktor), security (Encryption, Biometrics), and lifecycle management.
- **`:debug:devtool`**: On-device API monitor and shake-to-debug features.
- **`:debug:devtool-noop`**: Production "no-op" version of the devtools.

---

## 🚀 How to Import into your Project

Since this is a library repository, you can integrate it into your existing KMP project using **Git Submodules**.

### 1. Add as a Submodule
In your main project's terminal, run:

```bash
git submodule add <this-repo-url> libs/kmp-core
```

### 2. Configure `settings.gradle.kts`
Include the modules from the submodule directory:

```kotlin
// In your main project's settings.gradle.kts
include(":shared-core")
project(":shared-core").projectDir = file("libs/kmp-core/shared/core")

include(":debug-devtool")
project(":debug-devtool").projectDir = file("libs/kmp-core/debug/devtool")

include(":debug-devtool-noop")
project(":debug-devtool-noop").projectDir = file("libs/kmp-core/debug/devtool-noop")
```

### 3. Add Dependencies
In your app or feature modules' `build.gradle.kts`:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(project(":shared-core"))
        
        // Conditional devtool integration
        if (isDebug) { // Your debug flag
            implementation(project(":debug-devtool"))
        } else {
            implementation(project(":debug-devtool-noop"))
        }
    }
}
```

---

## 📚 Documentation
For more details on how to use the core features and debugging tools, see:
- [**Core Module Guide**](./docs/README-core.md)
- [**Debug Module Guide**](./docs/README-debug.md)
- [**Environment Setup**](./docs/ENV_SETUP.md)
- [**Gradle Guide**](./docs/GRADLE_GUIDE.md)
