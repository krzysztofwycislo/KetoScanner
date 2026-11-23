package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.product.api.model.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelNotesTest {

    @Test
    fun `laxative and other warnings are dropped`() {
        assertDropped(
            "Un consumo excesivo puede tener efectos laxantes",
            "consumo excesivo puede tener efectos laxantes",
            "Un consumo eccessivo può avere effetti lassativi",
            "Kann bei übermäßigem Verzehr abführend wirken",
            "Übermäßiger Verzehr kann abführend wirken",
            "Spożycie w nadmiernych ilościach może mieć efekt przeczyszczający",
            "Contains a source of phenylalanine",
        )
    }

    @Test
    fun `may contain lines are dropped`() {
        assertDropped(
            "May contain nuts",
            "(May contain traces of milk",
            "Kann Spuren von Nüssen enthalten",
            "Peut contenir des fruits à coque",
            "Traces d'arachides",
            "Traces d’arachides",
            "Może zawierać orzechy",
            "PUÒ CONTENERE LATTE",
        )
    }

    @Test
    fun `texts without a letter and storage advice are dropped`() {
        assertDropped(
            "99‚67 5",
            "2",
            "✔",
            "Store in a cool dry place",
            "À conserver au sec",
            "Przechowywać w suchym miejscu",
        )
    }

    @Test
    fun `a Portuguese line about allergens in bold is dropped`() {
        assertDropped(
            "Os ingredientes em negrito contém alergénios Pode conter vestígios de outros frutos de casca rija",
        )
    }

    @Test
    fun `an Italian allergen line is dropped`() {
        assertDropped("ALLERGENI Contiene Soia Potrebbe contenere Latte Contiene Grano")
    }

    @Test
    fun `an English line about allergens in bold is dropped`() {
        assertDropped("bold ingredients contain allergens")
    }

    @Test
    fun `an Italian line about highlighted allergens is dropped`() {
        assertDropped("Gli ingredienti evidenziati rendono il prodotto non idoneo al consumo")
    }

    @Test
    fun `a line about the production unit is dropped`() {
        assertDropped("manufactured in a unit that uses as raw materials")
    }

    @Test
    fun `an Italian line about the best way to store is dropped`() {
        assertDropped("Per conservare al meglio il prodotto si consiglia di riporlo in luogo fresco e asciutto")
    }

    @Test
    fun `real ingredients that only resemble a note are kept`() {
        assertKept(
            "kakao",
            "kanel",
            "kandierte Früchte",
            "chocolate contains a minimum of 65% cocoa",
            "cocoa may contain nuts",
            "Assaisonnement crème acidulée",
            "Natives Olivenöl",
            "konserwowany ogórek",
            "cocoa manufactured in a unit that uses nuts",
            "acido ascorbico per conservare al meglio",
            "boldo",
        )
    }

    @Test
    fun `allergens after a line about the production unit are dropped`() {
        assertEquals(
            listOf("en:cocoa"),
            idsWithoutNotes(
                ingredient("en:cocoa"),
                note("manufactured in a unit that uses as raw materials"),
                ingredient("en:peanut"),
                ingredient("en:almond"),
            ),
        )
    }

    @Test
    fun `allergens after a Portuguese line about allergens in bold are dropped`() {
        assertEquals(
            listOf("en:oat"),
            idsWithoutNotes(
                ingredient("en:oat"),
                note("Os ingredientes em negrito contém alergénios Pode conter vestígios"),
                ingredient("en:soya"),
            ),
        )
    }

    @Test
    fun `allergens after a may contain line are dropped`() {
        assertEquals(
            listOf("en:cocoa"),
            idsWithoutNotes(
                ingredient("en:cocoa"),
                note("may contain nuts"),
                ingredient("en:soya"),
                ingredient("en:sesame"),
            ),
        )
    }

    @Test
    fun `allergens before a may contain line are kept`() {
        assertEquals(
            listOf("en:milk", "en:cocoa"),
            idsWithoutNotes(
                ingredient("en:milk"),
                ingredient("en:cocoa"),
                note("may contain nuts"),
                ingredient("en:soya"),
            ),
        )
    }

    @Test
    fun `allergens are kept when a real ingredient follows the may contain line`() {
        assertEquals(
            listOf("en:cocoa", "en:milk", "en:sugar"),
            idsWithoutNotes(
                ingredient("en:cocoa"),
                note("may contain nuts"),
                ingredient("en:milk"),
                ingredient("en:sugar"),
            ),
        )
    }

    @Test
    fun `allergens after the last may contain line are dropped when a real ingredient follows an earlier one`() {
        assertEquals(
            listOf("en:milk", "en:sugar"),
            idsWithoutNotes(
                note("may contain nuts"),
                ingredient("en:milk"),
                ingredient("en:sugar"),
                note("may contain soya"),
                ingredient("en:sesame"),
            ),
        )
    }

    @Test
    fun `an unknown id after a may contain line is kept and does not stop the allergens from being dropped`() {
        assertEquals(
            listOf("en:cocoa", "es:amendoins"),
            idsWithoutNotes(
                ingredient("en:cocoa"),
                note("may contain nuts"),
                ingredient("es:amendoins"),
                ingredient("en:soya"),
            ),
        )
    }

    @Test
    fun `an allergen with a percent or with nested ingredients is kept after a may contain line`() {
        assertEquals(
            listOf("en:milk", "en:mustard"),
            idsWithoutNotes(
                note("may contain nuts"),
                ingredient(id = "en:milk", percent = 3.0),
                ingredient(id = "en:mustard", sub = listOf(ingredient("en:water"))),
                ingredient("en:soya"),
            ),
        )
    }

    private fun assertDropped(vararg labelNames: String) {
        assertEquals(emptyList(), labelNames.map(::note).withoutLabelNotes().map { it.labelName })
    }

    private fun assertKept(vararg labelNames: String) {
        assertEquals(labelNames.toList(), labelNames.map(::note).withoutLabelNotes().map { it.labelName })
    }

    private fun idsWithoutNotes(vararg ingredients: Ingredient): List<String> =
        ingredients.toList().withoutLabelNotes().map { it.id }

    private fun note(labelName: String): Ingredient = ingredient("xx:$labelName").copy(
        name = labelName,
        labelName = labelName,
    )
}
