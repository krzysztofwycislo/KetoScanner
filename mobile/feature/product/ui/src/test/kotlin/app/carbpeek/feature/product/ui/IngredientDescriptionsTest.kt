package app.carbpeek.feature.product.ui

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IngredientDescriptionsTest {

    private val names = R.string::class.java.fields.associate { it.getInt(null) to it.name }
    private val texts = readTexts()
    private val ids = ingredientDescriptions.flatMap { it.ids }

    @Test
    fun `the ids texts and sources are the accepted ones row by row`() {
        val accepted = readAccepted()
        val built = ingredientDescriptions.map { description ->
            listOf(
                description.ids.joinToString(","),
                text(description.what),
                text(description.does),
                description.sources.joinToString("; "),
            ).joinToString("\t")
        }

        assertEquals(143, accepted.size)
        assertEquals(accepted, built)
    }

    @Test
    fun `every id is an English one and is used once`() {
        assertEquals(236, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { id ->
            assertTrue(id.matches(Regex("en:[a-z0-9-]+")), id)
        }
    }

    @Test
    fun `every entry has a source and a text`() {
        ingredientDescriptions.forEach { description ->
            assertTrue(description.sources.isNotEmpty(), "${description.ids}")
            assertTrue(description.what != null || description.does != null, "${description.ids}")
        }
    }

    @Test
    fun `the additives that wait for a safety note have no entry`() {
        assertFalse("en:e250" in ids)
        assertFalse("en:e223" in ids)
    }

    @Test
    fun `every text is used and there is no other one`() {
        val used = ingredientDescriptions
            .flatMap { listOfNotNull(it.what, it.does) }
            .map { names[it] }
            .toSet()

        assertEquals(162, used.size)
        assertEquals<Set<String?>>(texts.keys, used)
    }

    @Test
    fun `no two texts are the same and none has a long dash`() {
        assertEquals(texts.size, texts.values.toSet().size)
        texts.forEach { (name, text) ->
            assertFalse('–' in text || '—' in text, name)
        }
    }

    private fun text(id: Int?): String =
        if (id == null) {
            "-"
        } else {
            assertNotNull(texts[names[id]], names[id])
        }

    private fun readAccepted(): List<String> =
        assertNotNull(javaClass.getResourceAsStream("/ingredient-descriptions.tsv"))
            .bufferedReader()
            .use { it.readLines() }

    private fun readTexts(): Map<String, String> {
        val strings = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(File("src/main/res/values/strings.xml"))
            .getElementsByTagName("string")
        return (0 until strings.length)
            .map { strings.item(it) as Element }
            .filter { it.getAttribute("name").startsWith("product_ingredient_about_") }
            .associate { it.getAttribute("name") to it.textContent.replace("\\'", "'") }
    }
}
