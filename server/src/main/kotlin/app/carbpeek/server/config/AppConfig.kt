package app.carbpeek.server.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class AppConfig(
    val port: Int,
    val environment: Environment,
    val database: DatabaseConfig,
    val meta: MetaConfig,
    val product: ProductConfig,
    val http: HttpConfig,
) {
    companion object {
        fun fromEnvironment(env: Map<String, String>): AppConfig = EnvironmentReader(env).read()
    }
}

enum class Environment {
    DEV,
    STAGING,
    PROD,
}

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val poolSize: Int,
) {
    override fun toString(): String = "DatabaseConfig(url=$url, user=$user, password=***, poolSize=$poolSize)"
}

data class MetaConfig(
    val serverVersion: String,
    val minSupportedVersionCode: Int,
)

data class ProductConfig(
    val freshFor: Duration,
    val notFoundFreshFor: Duration,
    val offRequestsPerMinute: Int,
)

data class HttpConfig(
    val productRequestsPerMinutePerIp: Int,
    val behindCloudflare: Boolean,
)

private const val DEV_DB_URL = "jdbc:postgresql://localhost:5432/carbpeek"
private const val DEV_DB_CREDENTIAL = "carbpeek"

private class EnvironmentReader(private val env: Map<String, String>) {
    private val problems = mutableListOf<String>()

    fun read(): AppConfig {
        val port = int(name = "PORT", default = 8080)
        val environment = environment()
        val isDev = environment == Environment.DEV
        val database = DatabaseConfig(
            url = connection(name = "DB_URL", devDefault = DEV_DB_URL, isDev = isDev),
            user = connection(name = "DB_USER", devDefault = DEV_DB_CREDENTIAL, isDev = isDev),
            password = connection(name = "DB_PASSWORD", devDefault = DEV_DB_CREDENTIAL, isDev = isDev),
            poolSize = int(name = "DB_POOL_SIZE", default = 10),
        )
        val meta = MetaConfig(
            serverVersion = value("SERVER_VERSION") ?: "dev",
            minSupportedVersionCode = int(name = "MIN_SUPPORTED_VERSION_CODE", default = 0),
        )
        val product = ProductConfig(
            freshFor = positiveInt(name = "PRODUCT_FRESH_HOURS", default = 168).hours,
            notFoundFreshFor = positiveInt(name = "PRODUCT_NOT_FOUND_HOURS", default = 24).hours,
            offRequestsPerMinute = positiveInt(name = "OFF_REQUESTS_PER_MINUTE", default = 10),
        )
        val http = HttpConfig(
            productRequestsPerMinutePerIp = positiveInt(name = "PRODUCT_REQUESTS_PER_MINUTE_PER_IP", default = 60),
            behindCloudflare = boolean(name = "BEHIND_CLOUDFLARE", default = false),
        )
        check(problems.isEmpty()) { "Invalid server configuration: ${problems.joinToString(separator = "; ")}" }
        return AppConfig(
            port = port,
            environment = environment ?: Environment.DEV,
            database = database,
            meta = meta,
            product = product,
            http = http,
        )
    }

    private fun value(name: String): String? = env[name]?.takeIf { it.isNotBlank() }

    private fun int(
        name: String,
        default: Int,
    ): Int {
        val raw = value(name) ?: return default
        return raw.toIntOrNull() ?: default.also { problems += "$name is not a number" }
    }

    private fun positiveInt(
        name: String,
        default: Int,
    ): Int {
        val number = int(name = name, default = default)
        if (number < 1) {
            problems += "$name must be at least 1"
        }
        return number
    }

    private fun boolean(
        name: String,
        default: Boolean,
    ): Boolean {
        val raw = value(name) ?: return default
        return raw.toBooleanStrictOrNull() ?: default.also { problems += "$name must be true or false" }
    }

    private fun environment(): Environment? {
        val raw = value("ENVIRONMENT") ?: return Environment.DEV
        val environment = Environment.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        if (environment == null) {
            problems += "ENVIRONMENT must be one of dev, staging, prod"
        }
        return environment
    }

    private fun connection(
        name: String,
        devDefault: String,
        isDev: Boolean,
    ): String {
        val raw = value(name)
        return when {
            raw != null -> {
                raw
            }

            isDev -> {
                devDefault
            }

            else -> {
                problems += "$name is required"
                ""
            }
        }
    }
}
