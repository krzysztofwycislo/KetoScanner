package pl.morning.tower.karbo.core.designsystem.text

import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.model.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryLabelsTest {

    @Test
    fun `other maps to the other label`() {
        assertEquals(UiText.Res(R.string.designsystem_category_other), ProductCategory.OTHER.label)
    }

    @Test
    fun `grains map to the grains label`() {
        assertEquals(UiText.Res(R.string.designsystem_category_grains), ProductCategory.GRAINS.label)
    }
}
