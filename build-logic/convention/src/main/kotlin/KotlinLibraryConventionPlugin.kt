import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Plain Kotlin/JVM library, configured like `domain` (JVM 17). Tests use kotlin.test on JUnit4.
 * Code in these modules avoids java.* / android.* so a later KMP move only changes Gradle files.
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")

        project.extensions.configure(JavaPluginExtension::class.java) {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        project.extensions.configure(KotlinJvmProjectExtension::class.java) {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        project.dependencies.add("testImplementation", project.dependencies.kotlin("test"))

        project.tasks.withType(Test::class.java).configureEach {
            useJUnit()
        }
    }
}
