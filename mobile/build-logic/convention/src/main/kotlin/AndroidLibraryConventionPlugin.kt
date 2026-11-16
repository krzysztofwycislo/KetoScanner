import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Shared Android library setup: compileSdk and minSdk from the version catalog, no minification, Java 17.
 * Modules set only their namespace and dependencies.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("com.android.library")

        val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        val compileSdkVersion = libs.findVersion("compileSdk").get().requiredVersion.toInt()
        val minSdkVersion = libs.findVersion("minSdk").get().requiredVersion.toInt()

        project.extensions.configure(LibraryExtension::class.java) {
            compileSdk = compileSdkVersion

            defaultConfig {
                minSdk = minSdkVersion
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}
