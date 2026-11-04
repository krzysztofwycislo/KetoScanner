package pl.morning.tower.karbo.core.designsystem.text

import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.model.ProductCategory

val ProductCategory.label: UiText
    get() = UiText.Res(
        when (this) {
            ProductCategory.SPREADS -> R.string.designsystem_category_spreads
            ProductCategory.OILS_AND_SAUCES -> R.string.designsystem_category_oils_and_sauces
            ProductCategory.WATER -> R.string.designsystem_category_water
            ProductCategory.DRINKS -> R.string.designsystem_category_drinks
            ProductCategory.MILK_AND_JUICE -> R.string.designsystem_category_milk_and_juice
            ProductCategory.YOGURT_AND_CREAM -> R.string.designsystem_category_yogurt_and_cream
            ProductCategory.CHOCOLATE_AND_BARS -> R.string.designsystem_category_chocolate_and_bars
            ProductCategory.CHEESE -> R.string.designsystem_category_cheese
            ProductCategory.EGGS -> R.string.designsystem_category_eggs
            ProductCategory.FISH -> R.string.designsystem_category_fish
            ProductCategory.NUTS_AND_SEEDS -> R.string.designsystem_category_nuts_and_seeds
            ProductCategory.GRAINS -> R.string.designsystem_category_grains
            ProductCategory.BISCUITS -> R.string.designsystem_category_biscuits
            ProductCategory.TEA_AND_GREENS -> R.string.designsystem_category_tea_and_greens
            ProductCategory.MEAT -> R.string.designsystem_category_meat
            ProductCategory.OTHER -> R.string.designsystem_category_other
        },
    )
