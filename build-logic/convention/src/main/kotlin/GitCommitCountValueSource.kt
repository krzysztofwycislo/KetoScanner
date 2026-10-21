import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations

interface GitCommitCountParams : ValueSourceParameters {
    val repoDir: DirectoryProperty
}

/** Returns the number of commits reachable from HEAD, or null when git can't provide a reliable count. */
abstract class GitCommitCountValueSource : ValueSource<Int, GitCommitCountParams> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): Int? {
        val logger = Logging.getLogger(GitCommitCountValueSource::class.java)
        val fallback = "falling back to commit count 0 (versionCode 1)"
        return try {
            val shallow = runGit("rev-parse", "--is-shallow-repository")
            if (shallow == null || shallow == "true") {
                logger.warn("KetoScanner version: not a usable git repository or shallow clone, $fallback.")
                return null
            }
            val count = runGit("rev-list", "--count", "HEAD")?.toIntOrNull()
            if (count == null || count <= 0) {
                logger.warn("KetoScanner version: could not read git commit count, $fallback.")
                return null
            }
            count
        } catch (e: Exception) {
            logger.warn("KetoScanner version: git failed (${e.message}), $fallback.")
            null
        }
    }

    /** Runs git in the repo dir and returns trimmed stdout, or null on a non-zero exit value. */
    private fun runGit(vararg args: String): String? {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            workingDir = parameters.repoDir.get().asFile
            commandLine(listOf("git") + args)
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        return if (result.exitValue == 0) stdout.toString().trim() else null
    }
}
