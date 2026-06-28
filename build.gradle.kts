// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
}

fun isWindows(): Boolean {
    return System.getProperty("os.name").lowercase().contains("windows")
}

fun scriptCommand(scriptName: String): List<String> {
    return if (isWindows()) {
        listOf("powershell", "-ExecutionPolicy", "Bypass", "-File", "$rootDir/scripts/$scriptName.ps1")
    } else {
        listOf("bash", "$rootDir/scripts/$scriptName.sh")
    }
}

tasks.register<Exec>("downloadModels") {
    group = "models"
    description = "Downloads models for development"

    commandLine(scriptCommand("download-models"))
}

tasks.register<Exec>("pushModels") {
    group = "models"
    description = "Pushes models to connected android device"

    mustRunAfter("downloadModels")
    commandLine(scriptCommand("push-models"))
}

tasks.register("downloadAndPushModels") {
    group = "models"
    description = "Downloads models and pushes them to connected android device"

    dependsOn("downloadModels", "pushModels")
}

tasks.register("connectedModelProbeAndroidTest") {
    group = "verification"
    description = "Installs debug app, pushes models, and runs the opt-in physical model coexistence smoke test"

    dependsOn(":app:installDebug", "pushModels", ":app:connectedDebugAndroidTest")
}

gradle.projectsEvaluated {
    tasks.named("pushModels").configure {
        mustRunAfter(":app:installDebug")
    }
    project(":app").tasks.named("connectedDebugAndroidTest").configure {
        mustRunAfter(tasks.named("pushModels"))
    }
}
