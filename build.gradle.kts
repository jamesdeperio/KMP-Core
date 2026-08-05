plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    format("misc") {
        target("**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

// Aggregated test tasks
tasks.register("testAll") {
    group = "verification"
    description = "Runs all tests across all modules."
    subprojects {
        tasks.findByName("allTests")?.let {
            this@register.dependsOn(it)
        }
    }
}

tasks.register("testFeatures") {
    group = "verification"
    description = "Runs all tests in feature modules."
    subprojects {
        if (path.startsWith(":shared:feature:")) {
            tasks.findByName("allTests")?.let {
                this@register.dependsOn(it)
            }
        }
    }
}

// Aggregated Lint tasks
tasks.register("lintAll") {
    group = "verification"
    description = "Runs lint across all modules."
    subprojects {
        tasks.matching { it.name == "lint" }.configureEach {
            this@register.dependsOn(this)
        }
    }
}

// Aggregated Detekt tasks
tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt across all modules."
    subprojects {
        tasks.matching { it.name == "detekt" }.configureEach {
            this@register.dependsOn(this)
        }
    }
}

// Aggregated Spotless tasks
tasks.register("spotlessApplyAll") {
    group = "verification"
    description = "Runs spotlessApply across all modules."
    dependsOn(tasks.matching { it.name == "spotlessApply" })
    subprojects {
        tasks.matching { it.name == "spotlessApply" }.configureEach {
            this@register.dependsOn(this)
        }
    }
}

tasks.register("spotlessCheckAll") {
    group = "verification"
    description = "Runs spotlessCheck across all modules."
    dependsOn(tasks.matching { it.name == "spotlessCheck" })
    subprojects {
        tasks.matching { it.name == "spotlessCheck" }.configureEach {
            this@register.dependsOn(this)
        }
    }
}

// Sequence of verification tasks
tasks.register("verifyProject") {
    group = "verification"
    description = "Runs all formatting, quality, and lint checks in sequence."
    dependsOn("spotlessApplyAll", "detektAll", "lintAll")
}

// Enforce ordering: Format -> Quality -> Lint
tasks.named("detektAll") { mustRunAfter("spotlessApplyAll") }
tasks.named("lintAll") { mustRunAfter("detektAll") }

// Consolidated Test Report
tasks.register<TestReport>("testReport") {
    group = "verification"
    description = "Generates a consolidated HTML test report."

    destinationDirectory.set(file("${layout.buildDirectory.get().asFile}/reports/all-tests"))

    // Collect results from all modules by searching the file system for binary results
    subprojects.forEach { sub ->
        val binaryResultsDir = file("${sub.buildDir}/test-results")
        if (binaryResultsDir.exists()) {
            binaryResultsDir.walkTopDown().filter { it.name == "binary" }.forEach {
                testResults.from(it)
            }
        }

        // Ensure we depend on the test tasks so they run first if needed
        sub.tasks.matching { it.name.contains("Test") }.configureEach {
            this@register.dependsOn(this)
        }
    }

    doLast {
        val reportFile = destinationDirectory.get().asFile.resolve("index.html")
        if (reportFile.exists()) {
            val os = System.getProperty("os.name").lowercase()
            val command =
                when {
                    os.contains("mac") -> arrayOf("open", reportFile.absolutePath)
                    os.contains("win") -> arrayOf("cmd", "/c", "start", reportFile.absolutePath)
                    else -> arrayOf("xdg-open", reportFile.absolutePath)
                }
            try {
                Runtime.getRuntime().exec(command)
                println("Report opened at: ${reportFile.absolutePath}")
            } catch (e: Exception) {
                println("Failed to open report: ${e.message}")
            }
        }
    }
}
