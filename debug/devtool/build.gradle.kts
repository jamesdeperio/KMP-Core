import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    android {
        namespace = "io.github.jamesdeperio.devtool"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    val xcfName = "debug:devtoolKit"

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinStdlib)
                implementation(libs.composeRuntime)
                implementation(libs.composeFoundation)
                implementation(libs.composeMaterial3)
                implementation(libs.kotlinxDatetime)
                implementation(libs.kotlinxSerializationJson)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidxAppcompat)
                implementation(libs.androidxActivityCompose)
                implementation(libs.androidxCoreKtx)
                implementation(libs.composeUi)
                implementation(libs.composeUiToolingPreview)
            }
        }

        iosMain {
            dependencies {
                // inherited
            }
        }
    }
}
