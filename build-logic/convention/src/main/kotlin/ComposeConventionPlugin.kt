import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        project.pluginManager.withPlugin("com.android.library") { enableCompose(project) }
        project.pluginManager.withPlugin("com.android.application") { enableCompose(project) }
    }

    private fun enableCompose(project: Project) {
        project.extensions.getByType(CommonExtension::class.java).buildFeatures.compose = true

        val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        val bom = libs.findLibrary("androidx-compose-bom").get()

        project.dependencies.add("implementation", project.dependencies.platform(bom))
        project.dependencies.add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        project.dependencies.add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
