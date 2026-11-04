package pl.morning.tower.karbo.core.designsystem.icon

import pl.morning.tower.karbo.core.model.ProductCategory

val ProductCategory.icon: AppIcon
    get() = when (this) {
        ProductCategory.SPREADS -> AppIcons.Jar
        ProductCategory.OILS_AND_SAUCES -> AppIcons.Bottle
        ProductCategory.WATER -> AppIcons.Bottle
        ProductCategory.DRINKS -> AppIcons.Can
        ProductCategory.MILK_AND_JUICE -> AppIcons.Carton
        ProductCategory.YOGURT_AND_CREAM -> AppIcons.Cup
        ProductCategory.CHOCOLATE_AND_BARS -> AppIcons.Bar
        ProductCategory.CHEESE -> AppIcons.Cheese
        ProductCategory.EGGS -> AppIcons.Egg
        ProductCategory.FISH -> AppIcons.Fish
        ProductCategory.NUTS_AND_SEEDS -> AppIcons.Nut
        ProductCategory.GRAINS -> AppIcons.Grain
        ProductCategory.BISCUITS -> AppIcons.Cookie
        ProductCategory.TEA_AND_GREENS -> AppIcons.Leaf
        ProductCategory.MEAT -> AppIcons.Meat
        ProductCategory.OTHER -> AppIcons.Bag
    }
