import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            extensions.configure(DetektExtension::class.java) {
                config.setFrom(rootProject.file("config/detekt/detekt.yml"))
                buildUponDefaultConfig.set(true)
                autoCorrect.set(false)
            }
        }
    }
}
