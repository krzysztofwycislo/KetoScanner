plugins {
    id("karbo.kotlin.library")
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":feature:analysis:api"))
}
