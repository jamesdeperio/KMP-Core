
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildkonfig)
}

// Helper to load properties with hierarchical overrides:
// 1. Env Variables (CI/CD)
// 2. Project Properties (-Pkey=value)
// 3. secrets.properties (Local machine secrets, git-ignored)
// 4. <env>.properties (Environment defaults)
fun getSecret(key: String, defaultValue: String, envFile: String): String {
    val properties = Properties()
    
    // Load environment-specific defaults first
    val targetFile = rootProject.file(envFile)
    if (targetFile.exists()) {
        targetFile.inputStream().use { properties.load(it) }
    }
    
    // Override with local secrets if they exist
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { properties.load(it) }
    }

    return System.getenv(key) 
        ?: project.findProperty(key)?.toString() 
        ?: properties.getProperty(key) 
        ?: defaultValue
}

val activeEnv = project.findProperty("env")?.toString() ?: "local"
val activeEnvFile = when (activeEnv) {
    "local" -> "local_env.properties"
    "prod" -> "prod.properties"
    else -> "nonprod.properties"
}

buildkonfig {
    packageName = "shared.core"

    defaultConfigs {
        buildConfigField(STRING, "BASE_URL", getSecret("BASE_URL", "NO_DEFAULT_URL", activeEnvFile))
        buildConfigField(BOOLEAN, "PINNING_ENABLED", getSecret("PINNING_ENABLED", "false", activeEnvFile))
        buildConfigField(BOOLEAN, "ENCRYPTION_ENABLED", getSecret("PAYLOAD_ENCRYPTION_ENABLED", "true", activeEnvFile))
        buildConfigField(BOOLEAN, "IS_DEBUG", getSecret("IS_DEBUG", "true", activeEnvFile))
    }

    targetConfigs {
        create("local") {
            buildConfigField(STRING, "BASE_URL", getSecret("BASE_URL", "NO_DEFAULT_URL", "local_env.properties"))
            buildConfigField(BOOLEAN, "PINNING_ENABLED", getSecret("PINNING_ENABLED", "false", "local_env.properties"))
            buildConfigField(BOOLEAN, "ENCRYPTION_ENABLED", getSecret("PAYLOAD_ENCRYPTION_ENABLED", "false", "local_env.properties"))
            buildConfigField(BOOLEAN, "IS_DEBUG", getSecret("IS_DEBUG", "true", "local_env.properties"))
        }
        create("prod") {
            buildConfigField(STRING, "BASE_URL", getSecret("BASE_URL", "NO_DEFAULT_URL", "prod.properties"))
            buildConfigField(BOOLEAN, "PINNING_ENABLED", getSecret("PINNING_ENABLED", "true", "prod.properties"))
            buildConfigField(BOOLEAN, "ENCRYPTION_ENABLED", getSecret("PAYLOAD_ENCRYPTION_ENABLED", "true", "prod.properties"))
            buildConfigField(BOOLEAN, "IS_DEBUG", getSecret("IS_DEBUG", "false", "prod.properties"))
        }
    }
}

kotlin {

    android {
        namespace = "shared.core"

        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }
    }

    val xcfName = "shared:coreKit"

    iosArm64 {
        binaries.framework { baseName = xcfName }
    }

    iosSimulatorArm64 {
        binaries.framework { baseName = xcfName }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinStdlib)
                implementation(libs.kotlinxDatetime)

                // Devtool - core depends on the swappable debug module
                // This allows HttpClient to call NetworkDebug.log directly.
                val isDebug = project.findProperty("IS_DEBUG")?.toString()?.toBoolean() ?: true
                if (isDebug) {
                    implementation(project(":debug:devtool"))
                } else {
                    implementation(project(":debug:devtool-noop"))
                }

                // Ktor client
                implementation(libs.ktorClientCore)
                implementation(libs.ktorClientContentNegotiation)
                implementation(libs.ktorSerializationKotlinxJson)
                implementation(libs.ktorClientLogging)
                implementation(libs.ktorClientAuth)

                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlinxCoroutinesCore)

                // Cross-platform crypto (AES-GCM, ECDH, HKDF, etc.)
                implementation(libs.cryptographyCore)
                implementation(libs.cryptographyProviderOptimal)

                // Secure Settings for Token storage
                implementation(libs.multiplatformSettings)
                implementation(libs.multiplatformSettingsCoroutines)

                // Koin DI
                implementation(libs.koinCore)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktorClientOkhttp)
                implementation(libs.okhttp)
                implementation(libs.koinAndroid)
                implementation(libs.androidxBiometric)
                implementation(libs.androidxAppcompat) // For FragmentActivity
                implementation(libs.androidxLifecycleProcess)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktorClientDarwin)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.testBundle)
                implementation(libs.kotlinxCoroutinesTest)
            }
        }
    }
}
