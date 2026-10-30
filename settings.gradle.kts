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
include(":app", ":openfoodapi", ":barcodescanner", ":core:common", ":core:model", ":core:testing", ":core:designsystem", ":core:database", ":core:datastore")
include(
    ":feature:product:api",
    ":feature:product:data",
    ":feature:preferences:api",
    ":feature:analysis:api",
    ":feature:history:api",
    ":feature:history:data",
    ":feature:history:ui",
    ":feature:home:ui",
    ":feature:product:ui",
    ":feature:scanner:ui",
    ":feature:favourites:api",
    ":feature:favourites:data",
    ":feature:favourites:ui",
    ":feature:preferences:data",
    ":feature:preferences:ui",
    ":feature:about:ui",
    ":feature:analysis:domain",
)
