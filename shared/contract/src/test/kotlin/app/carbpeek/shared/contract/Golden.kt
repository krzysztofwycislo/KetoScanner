package app.carbpeek.shared.contract

import kotlinx.serialization.json.JsonElement

fun readGolden(name: String): String {
    val resource = checkNotNull(Thread.currentThread().contextClassLoader.getResource("golden/v1/$name")) {
        "Missing golden file $name"
    }
    return resource.readText()
}

fun parseJson(text: String): JsonElement = ApiJson.parseToJsonElement(text)
