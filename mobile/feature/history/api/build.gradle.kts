plugins {
    id("carbpeek.kotlin.library")
}

dependencies {
    api(project(":feature:analysis:api"))
    api(project(":core:common"))
}
