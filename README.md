# 🛠 KMP Core & Debug Tools

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![KMP Core CI](https://github.com/jamesdeperio/KMP-Core/actions/workflows/ci.yml/badge.svg)](https://github.com/jamesdeperio/KMP-Core/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/jamesdeperio/KMP-Core.svg)](https://jitpack.io/#jamesdeperio/KMP-Core)

Foundational shared infrastructure, networking, security, permissions, and debugging tools for the **Kotlin Multiplatform (KMP)** ecosystem (Android & iOS).

---

## 📦 Modules

- **`:shared:core`**: Core networking (`HttpClientFactory`, `KmpNetworkConfig`, `ApiResult`, `AuthPlugin`), security (`RASP`, `BiometricAuthenticator`, `SecureStorage`, `PayloadCipher`), permissions (`PermissionManager`), and utilities.
- **`:debug:devtool`**: On-device API monitor, request/response inspector, and shake-to-debug features.
- **`:debug:devtool-noop`**: Production "no-op" zero-overhead release version of devtools.

---

## ✨ Features at a Glance

- 🌐 **Ktor Networking**: Generic `KmpNetworkConfig` setup, `ApiResult<T>` wrapper, automatic 401 token refresh (`AuthPlugin`), and payload decryption (`PayloadEncryptionPlugin`).
- 🔐 **Security & RASP**: Root/jailbreak detection, debugger check, emulator check, Android KeyStore / iOS Keychain `SecureStorage`, and biometrics (`LAContext` & `BiometricPrompt`).
- 🛠 **Permissions**: Unified `PermissionManager` for Camera, Location, Storage, and Notifications.
- 🧰 **Utilities**: `DateTimeUtils`, `ImageProcessor`, `FileStorage`, `JwtUtils`, `DataMasker`, `FlowExtensions`.

---

## 🚀 How to Import into your Project

### Option A: JitPack (Recommended)

#### 1. Add Repository
In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### 2. Add Dependencies
In your module's `build.gradle.kts`:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("com.github.jamesdeperio.KMP-Core:shared:core:Tag")
        
        // Optional DevTools
        if (isDebugMode) {
            implementation("com.github.jamesdeperio.KMP-Core:debug:devtool:Tag")
        } else {
            implementation("com.github.jamesdeperio.KMP-Core:debug:devtool-noop:Tag")
        }
    }
}
```

---

### Option B: Git Submodule

#### 1. Add as a Submodule
```bash
git submodule add https://github.com/jamesdeperio/KMP-Core.git libs/kmp-core
```

#### 2. Configure `settings.gradle.kts`
```kotlin
include(":shared-core")
project(":shared-core").projectDir = file("libs/kmp-core/shared/core")

include(":debug-devtool")
project(":debug-devtool").projectDir = file("libs/kmp-core/debug/devtool")

include(":debug-devtool-noop")
project(":debug-devtool-noop").projectDir = file("libs/kmp-core/debug/devtool-noop")
```

#### 3. Add Dependencies
```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(project(":shared-core"))
    }
}
```

---

## 📚 Documentation

For detailed item documentation and code examples:
- [**Core Module Guide**](./docs/README-core.md)
- [**Networking Guide**](./shared/core/docs/networking.md)
- [**Security & RASP Guide**](./shared/core/docs/security.md)
- [**Permissions Guide**](./shared/core/docs/permissions.md)
- [**Debug Module Guide**](./docs/README-debug.md)
- [**Environment Setup**](./docs/ENV_SETUP.md)
