// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
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

tasks.register<Exec>("downloadAndPushModels") {
    group = "models"
    description = "Downloads models and pushes them to connected android device"

    dependsOn("downloadModels", "pushModels")
}
