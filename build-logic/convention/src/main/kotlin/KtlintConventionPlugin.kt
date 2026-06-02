import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            extensions.configure(KtlintExtension::class.java) {
                debug.set(false)
                android.set(true)

                dependencies.add(
                    "ktlintRuleset",
                    "io.nlopez.compose.rules:ktlint:0.5.9",
                )
            }
        }
    }
}
