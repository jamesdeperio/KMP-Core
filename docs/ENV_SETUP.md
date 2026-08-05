# 🌍 Environment Setup Guide

This project uses a hierarchical configuration system to manage API URLs, security keys, and debug flags across different environments.

---

## 🛠 How it Works

We use **BuildKonfig** to generate a `shared.core.BuildKonfig` object that is accessible in the `commonMain` source set. The values for this object are pulled from several property files in the root directory.

### The Hierarchy (Priority Order)
1.  **Environment Variables**: Highest priority (used in CI/CD like GitHub Actions).
2.  **Project Properties**: Passed via command line (e.g., `./gradlew -PBASE_URL=...`).
3.  **`secrets.properties`**: Local machine secrets. **Never committed to Git.**
4.  **`<env>.properties`**: Default values for a specific environment (e.g., `prod.properties`).

---

## 🏗 Available Environments

We have three predefined environment files:

| File | Environment | Purpose |
| :--- | :--- | :--- |
| `local_env.properties` | **Local** | Default for daily development. Points to local/dev servers. |
| `nonprod.properties` | **Staging** | Used for testing and QA builds. |
| `prod.properties` | **Production** | Points to live servers. Includes strict security settings. |

---

## 🚀 Switching Environments

### 1. Permanent Default
You can set the default environment for your local machine in the `gradle.properties` file:

```properties
# Environment selection: local, nonprod, prod
env=local
```

### 2. Command Line Override
You can switch to a different environment on the fly using the `-Penv` flag:

```bash
# Run tests with Staging settings
../gradlew testAll -Penv=nonprod
```

---

## 🔐 Handling Secrets

**DO NOT** put sensitive keys (like API secret keys) in `prod.properties` or `nonprod.properties`.

### For Local Development:
1.  Create a file named `secrets.properties` in the project root.
2.  Add your secrets there. This file is already in `.gitignore`.
    ```properties
    API_KEY=your_secret_key_here
    ENCRYPTION_SECRET=something_very_secure
    ```

### For CI/CD (GitHub Actions):
Add the same keys as **Actions Secrets** in your repository settings. The build script will automatically pick them up from environment variables.

---

## 📝 Key Properties Explanation

| Property | Type | Description |
| :--- | :--- | :--- |
| `BASE_URL` | String | The root URL for all API calls. |
| `PINNING_ENABLED` | Boolean | If `true`, enables SSL Pinning (Certificate validation). |
| `PAYLOAD_ENCRYPTION_ENABLED` | Boolean | If `true`, encrypts request/response bodies. |
| `IS_DEBUG` | Boolean | Enables verbose logging and developer tools. |

---

## 💡 Troubleshooting
- **Configuration not updating?** Run `../gradlew clean` to force BuildKonfig to regenerate its classes.
- **Missing BuildKonfig?** BuildKonfig is generated during compilation. If your IDE shows red code, try running `../gradlew :shared:core:compileKotlinAndroid`.
