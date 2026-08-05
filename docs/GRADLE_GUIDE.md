# 🐘 Gradle Guide: Running Tasks

This guide explains the most common Gradle commands used in this project for building, testing, and maintaining code quality.

---

## 🛠 Basic Commands

| Task | Command | Description |
| :--- | :--- | :--- |
| **Clean** | `../gradlew clean` | Deletes the `build` folders and clears cache. |
| **Build All** | `../gradlew build` | Compiles every module and runs all tests. |
| **Assemble** | `../gradlew assemble` | Builds Frameworks/AARs without running tests. |

---

## ✨ Code Quality & Formatting

We use **Spotless** (formatting), **Detekt** (static analysis), and **Android Lint** to keep our code clean.

| Scope | Command | Description |
| :--- | :--- | :--- |
| **Full Verify** | `../gradlew verifyProject` | Runs Spotless Apply, Detekt, and Lint in sequence. |
| **Format** | `../gradlew spotlessApplyAll` | Automatically fixes code formatting issues. |
| **Analysis** | `../gradlew detektAll` | Checks for "code smells" and complex logic. |
| **Lint** | `../gradlew lintAll` | Runs Android's native lint checks. |

---

## 🧪 Testing

We have several layers of testing. You can run them individually or use our aggregated tasks.

### 1. General Testing
*   **Run All Tests**: `../gradlew testAll`
*   **Generate Report**: `../gradlew testReport` (Merges all test results into one HTML page)

---

## 🏗 Module Specifics

| Task | Command |
| :--- | :--- |
| **Build Core** | `../gradlew :shared:core:assemble` |
| **Test Core** | `../gradlew :shared:core:allTests` |

---

## 💡 Tips
- **Performance**: Use the `--parallel` flag for faster builds on multi-core machines.
- **Dry Run**: Use the `-m` flag (e.g., `../gradlew test -m`) to see which tasks will be executed without actually running them.
- **Stacktrace**: If a build fails, use `--stacktrace` or `--info` to see more detailed error logs.

---
> [!NOTE]
> Always run `../gradlew spotlessApplyAll` before committing code to pass the CI checks!
