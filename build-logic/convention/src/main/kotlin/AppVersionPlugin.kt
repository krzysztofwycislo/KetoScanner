import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Sets versionName = "<carbpeek.versionBase>.<git commit count>" and versionCode = git commit count
 * on every variant of an Android application module.
 * Resolved lazily, so a new commit does not invalidate the configuration cache.
 */
class AppVersionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            val providers = project.providers
            val gitCommitCount = providers.of(GitCommitCountValueSource::class.java) {
                parameters.repoDir.set(project.rootProject.layout.projectDirectory)
            }.orElse(0)
            val versionBase = providers.gradleProperty("carbpeek.versionBase").orElse("0.0.1")
            val appVersionName = versionBase.zip(gitCommitCount) { base, count -> "$base.$count" }
            val appVersionCode = gitCommitCount.map { maxOf(it, 1) }

            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java).onVariants { variant ->
                variant.outputs.forEach { output ->
                    output.versionCode.set(appVersionCode)
                    output.versionName.set(appVersionName)
                }
            }
        }
    }
}
