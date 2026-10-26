pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KetoScanner"
include(":app", ":openfoodapi", ":barcodescanner", ":core:common", ":core:model", ":core:testing", ":core:designsystem")
include(
    ":feature:product:api",
    ":feature:preferences:api",
    ":feature:analysis:api",
    ":feature:history:api",
    ":feature:favourites:api",
    ":feature:analysis:domain",
)
