import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class ModelsConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val downloadModels = tasks.register("downloadModels", Exec::class.java) {
            group = "models"
            description = "Downloads models for development"

            commandLine(scriptCommand("download-models"))
        }

        val pushModels = tasks.register("pushModels", Exec::class.java) {
            group = "models"
            description = "Pushes models to connected Android device"

            mustRunAfter(downloadModels)
            commandLine(scriptCommand("push-models"))
        }

        tasks.register("downloadAndPushModels") {
            group = "models"
            description = "Downloads models and pushes them to connected Android device"

            dependsOn(downloadModels, pushModels)
        }

        tasks.register("connectedModelProbeAndroidTest") {
            group = "verification"
            description =
                "Installs debug app, pushes models, and runs the opt-in physical model coexistence smoke test"

            dependsOn(
                ":app:installDebug",
                pushModels,
                ":app:connectedDebugAndroidTest"
            )
        }

        gradle.projectsEvaluated {
            pushModels.configure {
                mustRunAfter(":app:installDebug")
            }

            project(":app")
                .tasks
                .named("connectedDebugAndroidTest")
                .configure {
                    mustRunAfter(pushModels)
                }
        }
    }

    private fun Project.scriptCommand(scriptName: String): List<String> {
        val scriptPath = "${rootProject.rootDir}/scripts"

        return if (isWindows()) {
            listOf(
                "powershell",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                "$scriptPath/$scriptName.ps1"
            )
        } else {
            listOf(
                "bash",
                "$scriptPath/$scriptName.sh"
            )
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name")
            .lowercase()
            .contains("windows")
    }
}
