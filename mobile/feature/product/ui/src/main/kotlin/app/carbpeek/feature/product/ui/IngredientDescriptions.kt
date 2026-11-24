package app.carbpeek.feature.product.ui

import androidx.annotation.StringRes

private const val ADDITIVES_LAW_ANNEX_I = "Regulation (EC) No 1333/2008, Annex I"
private const val ADDITIVE_SPECIFICATIONS = "Regulation (EU) No 231/2012"
private const val FDA_TYPES = "FDA, Types of Food Ingredients"
private const val UK_LIST = "UK approved additives and E numbers"
private const val VITAMINS_LAW_ANNEX_II = "Regulation (EC) No 1925/2006, Annex II"
private const val EFSA_2017 = "EFSA 2017"
private const val EFSA_2020 = "EFSA 2020"
private const val EFSA_CARRAGEENAN =
    "EFSA 2018, re-evaluation of carrageenan (E 407) and processed Eucheuma seaweed (E 407a), " +
        "doi:10.2903/j.efsa.2018.5238"
private const val EFSA_2018 = "EFSA 2018"
private const val ADDITIVES_LAW_POLYOLS = "Regulation (EC) No 1333/2008, Annex II, Group IV"
private const val LABEL_LAW = "Regulation (EU) No 1169/2011"
private const val SUGARS_LAW = "Directive 2001/111/EC"
private const val NOVEL_FOODS_LIST = "Regulation (EU) 2017/2470"
private const val CFR_MALT = "21 CFR 184.1445"
private const val COCOA_LAW = "Directive 2000/36/EC"
private const val DRIED_MILK_LAW = "Directive 2001/114/EC"
private const val CASEIN_LAW = "Directive (EU) 2015/2203"
private const val CFR_WHEY = "21 CFR 184.1979"
private const val FLAVOURINGS_LAW = "Regulation (EC) No 1334/2008"
private const val EFSA_PHOSPHATES =
    "EFSA 2019, re-evaluation of phosphoric acid-phosphates - di-, tri- and polyphosphates (E 338-341, " +
        "E 343, E 450-452), doi:10.2903/j.efsa.2019.5674, Appendix A"
private const val CFR_YOGURT = "21 CFR 131.200"
private const val COFALEC = "COFALEC"
private const val CFR_LEAVENING = "21 CFR 170.3(o)(17)"
private const val EFSA_MUCORPEPSIN =
    "EFSA 2024, safety evaluation of the food enzyme mucorpepsin from the non-genetically modified " +
        "Rhizomucor miehei strain FRO, doi:10.2903/j.efsa.2024.8512"
private const val LABEL_LAW_ANNEX_XV = "Regulation (EU) No 1169/2011, Annex XV"
private const val FDA_FIBRE = "FDA, questions and answers on dietary fibre"

private val modifiedStarchCodes = (1404..1452).map { "en:e$it" }

internal data class IngredientDescription(
    val ids: List<String>,
    @StringRes val what: Int?,
    @StringRes val does: Int?,
    val sources: List<String>,
)

internal val ingredientDescriptions: List<IngredientDescription> = listOf(
    IngredientDescription(
        ids = listOf("en:acid"),
        what = null,
        does = R.string.product_ingredient_about_makes_food_more_acidic,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:acidity-regulator"),
        what = null,
        does = R.string.product_ingredient_about_controls_how_acidic_a_food_is,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:antioxidant"),
        what = null,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_going_rancid_changing,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:colour"),
        what = null,
        does = R.string.product_ingredient_about_adds_colour_restores,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:emulsifier"),
        what = null,
        does = R.string.product_ingredient_about_keeps_ingredients_dont_mix,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:gelling-agent"),
        what = null,
        does = R.string.product_ingredient_about_gives_food_texture_forming,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:raising-agent"),
        what = null,
        does = R.string.product_ingredient_about_gives_off_gas_makes,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:stabiliser"),
        what = null,
        does = R.string.product_ingredient_about_keeps_mixture_even_holds,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:thickener"),
        what = null,
        does = R.string.product_ingredient_about_makes_food_thicker,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:sweetener"),
        what = null,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:preservative"),
        what = null,
        does = R.string.product_ingredient_about_helps_food_keep_longer_protecting,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:flavour-enhancer"),
        what = null,
        does = R.string.product_ingredient_about_makes_taste_smell_already,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:humectant"),
        what = null,
        does = R.string.product_ingredient_about_keeps_food_drying_out,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:firming-agent"),
        what = null,
        does = R.string.product_ingredient_about_keeps_fruit_vegetables_firm_crisp,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:anti-caking-agent"),
        what = null,
        does = R.string.product_ingredient_about_keeps_particles_sticking_together,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:anti-foaming-agent"),
        what = null,
        does = R.string.product_ingredient_about_prevents_reduces_foam,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:emulsifying-salts"),
        what = null,
        does = R.string.product_ingredient_about_spreads_fat_other_parts,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:foaming-agent"),
        what = null,
        does = R.string.product_ingredient_about_helps_gas_spread_evenly,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:glazing-agent"),
        what = null,
        does = R.string.product_ingredient_about_gives_shine_protective_coating,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:flour-treatment-agent"),
        what = null,
        does = R.string.product_ingredient_about_improves_how_flour_dough,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:bulking-agent"),
        what = null,
        does = R.string.product_ingredient_about_adds_volume_without_adding,
        sources = listOf(ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e330"),
        what = R.string.product_ingredient_about_acid_also_lemon_juice,
        does = R.string.product_ingredient_about_controls_how_acidic_food_can,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e270"),
        what = R.string.product_ingredient_about_acid_made_fermentation_chemically,
        does = R.string.product_ingredient_about_controls_how_acidic_food_is,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e500"),
        what = R.string.product_ingredient_about_sodium_carbonates_one_them,
        does = R.string.product_ingredient_about_makes_baked_goods_rise_controls,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e500ii"),
        what = R.string.product_ingredient_about_baking_soda,
        does = R.string.product_ingredient_about_makes_baked_goods_rise,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e170"),
        what = R.string.product_ingredient_about_mineral_chalk_limestone,
        does = R.string.product_ingredient_about_can_colour_food_white,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES, VITAMINS_LAW_ANNEX_II),
    ),
    IngredientDescription(
        ids = listOf("en:e341i"),
        what = R.string.product_ingredient_about_calcium_compound,
        does = R.string.product_ingredient_about_makes_baked_goods_rise,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e290"),
        what = R.string.product_ingredient_about_carbon_dioxide_gas,
        does = R.string.product_ingredient_about_makes_drinks_fizzy,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e200"),
        what = null,
        does = R.string.product_ingredient_about_helps_food_keep_longer_protecting,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e202", "en:e211", "en:e282"),
        what = null,
        does = R.string.product_ingredient_about_helps_food_keep_longer_protecting,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e300"),
        what = R.string.product_ingredient_about_ascorbic_acid_vitamin_c,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_keeps_its_colour_changing_can,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I, VITAMINS_LAW_ANNEX_II, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e301"),
        what = R.string.product_ingredient_about_form_vitamin_c,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_keeps_its_colour_changing,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I, VITAMINS_LAW_ANNEX_II),
    ),
    IngredientDescription(
        ids = listOf("en:e304i"),
        what = R.string.product_ingredient_about_form_vitamin_c_joined,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_going_rancid,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I, VITAMINS_LAW_ANNEX_II),
    ),
    IngredientDescription(
        ids = listOf("en:e306"),
        what = R.string.product_ingredient_about_extract_vegetable_oils_rich,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_going_rancid,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e392"),
        what = R.string.product_ingredient_about_extracts_rosemary,
        does = R.string.product_ingredient_about_protects_food_oxygen_example_going_rancid,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e322"),
        what = R.string.product_ingredient_about_fatty_substances_plants_animals,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:soya-lecithin"),
        what = R.string.product_ingredient_about_fatty_substances_soya,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:sunflower-lecithin"),
        what = R.string.product_ingredient_about_fatty_substances_sunflower,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e471"),
        what = R.string.product_ingredient_about_made_fats_oils,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e476"),
        what = R.string.product_ingredient_about_made_castor_oil,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2017, "Regulation (EC) No 1333/2008, Annex III"),
    ),
    IngredientDescription(
        ids = listOf("en:e472b"),
        what = R.string.product_ingredient_about_made_fats_oils_lactic,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2020),
    ),
    IngredientDescription(
        ids = listOf("en:e472e"),
        what = R.string.product_ingredient_about_made_fats_oils_tartaric,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2020),
    ),
    IngredientDescription(
        ids = listOf("en:e472f"),
        what = R.string.product_ingredient_about_made_fats_oils_acetic,
        does = R.string.product_ingredient_about_helps_ingredients_oil_water,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2020),
    ),
    IngredientDescription(
        ids = listOf("en:e401"),
        what = R.string.product_ingredient_about_substance_brown_seaweed,
        does = R.string.product_ingredient_about_thickens_stabilises_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, "21 CFR 184.1724"),
    ),
    IngredientDescription(
        ids = listOf("en:e406"),
        what = R.string.product_ingredient_about_substance_red_seaweed,
        does = R.string.product_ingredient_about_thickens_food_forms_gel,
        sources = listOf(ADDITIVE_SPECIFICATIONS, "21 CFR 184.1115"),
    ),
    IngredientDescription(
        ids = listOf("en:e407"),
        what = R.string.product_ingredient_about_substance_red_seaweed,
        does = R.string.product_ingredient_about_thickens_food_forms_gel,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_CARRAGEENAN),
    ),
    IngredientDescription(
        ids = listOf("en:e410"),
        what = R.string.product_ingredient_about_ground_inner_part_carob,
        does = R.string.product_ingredient_about_thickens_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2017),
    ),
    IngredientDescription(
        ids = listOf("en:e412"),
        what = R.string.product_ingredient_about_ground_inner_part_guar,
        does = R.string.product_ingredient_about_thickens_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e414"),
        what = R.string.product_ingredient_about_dried_gum_stems_branches,
        does = R.string.product_ingredient_about_stabilises_thickens_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e415"),
        what = R.string.product_ingredient_about_gum_made_fermentation_bacteria,
        does = R.string.product_ingredient_about_thickens_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e418"),
        what = R.string.product_ingredient_about_gum_made_fermentation_bacteria,
        does = R.string.product_ingredient_about_forms_gel_thickens_stabilises,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2018),
    ),
    IngredientDescription(
        ids = listOf("en:e425"),
        what = R.string.product_ingredient_about_root_tuber_konjac_plant,
        does = R.string.product_ingredient_about_thickens_food_forms_gel,
        sources = listOf(ADDITIVE_SPECIFICATIONS),
    ),
    IngredientDescription(
        ids = listOf("en:e440"),
        what = R.string.product_ingredient_about_substance_plants_usually_citrus,
        does = R.string.product_ingredient_about_thickens_stabilises_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, "21 CFR 184.1588"),
    ),
    IngredientDescription(
        ids = listOf("en:e460"),
        what = R.string.product_ingredient_about_cellulose_plant_fibres,
        does = R.string.product_ingredient_about_gives_food_texture,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e461", "en:e464"),
        what = R.string.product_ingredient_about_cellulose_plant_fibres_chemically,
        does = R.string.product_ingredient_about_thickens_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS),
    ),
    IngredientDescription(
        ids = listOf("en:e466"),
        what = R.string.product_ingredient_about_cellulose_plant_fibres_chemically,
        does = R.string.product_ingredient_about_thickens_stabilises_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, EFSA_2018),
    ),
    IngredientDescription(
        ids = listOf("en:modified-starch", "en:modified-corn-starch") + modifiedStarchCodes,
        what = R.string.product_ingredient_about_starch_was_treated_chemically,
        does = R.string.product_ingredient_about_gives_food_texture,
        sources = listOf(ADDITIVES_LAW_ANNEX_I, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e100"),
        what = R.string.product_ingredient_about_colour_extracted_turmeric,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e150", "en:e150a"),
        what = R.string.product_ingredient_about_colour_made_heating_sugars,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e150b"),
        what = R.string.product_ingredient_about_colour_made_heating_sugars_sulphites,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e150c"),
        what = R.string.product_ingredient_about_colour_made_heating_sugars_ammonium,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e150d"),
        what = R.string.product_ingredient_about_colour_made_heating_sugars_sulphites_ammonium,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e160a"),
        what = R.string.product_ingredient_about_beta_carotene_similar_colours,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e160ai"),
        what = R.string.product_ingredient_about_beta_carotene_orange_colour,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e160c"),
        what = R.string.product_ingredient_about_colour_extracted_paprika,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e420", "en:e421", "en:e953", "en:e965", "en:e965ii", "en:e966", "en:e967"),
        what = R.string.product_ingredient_about_polyol_sugar_alcohol_eu,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVES_LAW_POLYOLS, ADDITIVE_SPECIFICATIONS, UK_LIST, LABEL_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:e968"),
        what = R.string.product_ingredient_about_polyol_sugar_alcohol_made,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVES_LAW_POLYOLS, ADDITIVE_SPECIFICATIONS, UK_LIST, LABEL_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:e950", "en:e951"),
        what = R.string.product_ingredient_about_sweetener_used_place_sugar_about,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e955"),
        what = R.string.product_ingredient_about_sweetener_used_place_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e960"),
        what = R.string.product_ingredient_about_sweet_substances_steviol_glycosides_stevia_plant_made,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e960a", "en:e960c", "en:e960d"),
        what = R.string.product_ingredient_about_sweet_substances_steviol_glycosides_stevia_plant_can,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVE_SPECIFICATIONS, UK_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:e960b"),
        what = R.string.product_ingredient_about_sweet_substances_steviol_glycosides_made,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(ADDITIVE_SPECIFICATIONS),
    ),
    IngredientDescription(
        ids = listOf("en:e621"),
        what = R.string.product_ingredient_about_monosodium_glutamate_known_msg,
        does = R.string.product_ingredient_about_makes_taste_already_there,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e627", "en:e631"),
        what = null,
        does = R.string.product_ingredient_about_makes_taste_already_there,
        sources = listOf(FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e422"),
        what = R.string.product_ingredient_about_clear_thick_liquid_made,
        does = R.string.product_ingredient_about_keeps_food_moist,
        sources = listOf(ADDITIVE_SPECIFICATIONS, FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e509"),
        what = null,
        does = R.string.product_ingredient_about_keeps_fruit_vegetables_firm,
        sources = listOf(FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e551"),
        what = null,
        does = R.string.product_ingredient_about_keeps_powder_clumping,
        sources = listOf(FDA_TYPES, ADDITIVES_LAW_ANNEX_I),
    ),
    IngredientDescription(
        ids = listOf("en:e1200"),
        what = R.string.product_ingredient_about_glucose_units_joined_into,
        does = R.string.product_ingredient_about_adds_volume,
        sources = listOf(ADDITIVE_SPECIFICATIONS, "EFSA 2021"),
    ),
    IngredientDescription(
        ids = listOf("en:sugar"),
        what = R.string.product_ingredient_about_sugar_sugar_cane_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW, "21 CFR 184.1854", FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:dextrose", "en:glucose", "en:grape-sugar"),
        what = R.string.product_ingredient_about_glucose_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:fructose"),
        what = R.string.product_ingredient_about_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:glucose-syrup"),
        what = R.string.product_ingredient_about_sugar_syrup_made_starch,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW, FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:glucose-fructose-syrup", "en:fructose-glucose-syrup"),
        what = R.string.product_ingredient_about_glucose_syrup_also_contains,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:invert-sugar-syrup"),
        what = R.string.product_ingredient_about_sugar_syrup_which_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(SUGARS_LAW, "21 CFR 184.1859"),
    ),
    IngredientDescription(
        ids = listOf("en:isomaltulose"),
        what = R.string.product_ingredient_about_sugar_made_table_sugar,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(NOVEL_FOODS_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:trehalose"),
        what = R.string.product_ingredient_about_sugar_made_starch_table,
        does = R.string.product_ingredient_about_adds_sweetness,
        sources = listOf(NOVEL_FOODS_LIST),
    ),
    IngredientDescription(
        ids = listOf("en:isomalto-oligosaccharide"),
        what = R.string.product_ingredient_about_short_chains_glucose_mixed,
        does = null,
        sources = listOf(NOVEL_FOODS_LIST, "EFSA 2024"),
    ),
    IngredientDescription(
        ids = listOf("en:barley-malt-extract"),
        what = R.string.product_ingredient_about_sweet_extract_sprouted_barley,
        does = R.string.product_ingredient_about_gives_flavour,
        sources = listOf(CFR_MALT),
    ),
    IngredientDescription(
        ids = listOf("en:malt-extract"),
        what = R.string.product_ingredient_about_sweet_extract_sprouted_grain,
        does = R.string.product_ingredient_about_gives_flavour,
        sources = listOf(CFR_MALT),
    ),
    IngredientDescription(
        ids = listOf("en:maltodextrin"),
        what = R.string.product_ingredient_about_starch_was_broken_down_into,
        does = R.string.product_ingredient_about_not_sweet,
        sources = listOf("21 CFR 184.1444", LABEL_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:dextrin"),
        what = R.string.product_ingredient_about_starch_was_broken_down_dry,
        does = R.string.product_ingredient_about_thickens_food,
        sources = listOf("21 CFR 184.1277"),
    ),
    IngredientDescription(
        ids = listOf("en:wheat-gluten"),
        what = R.string.product_ingredient_about_main_protein_wheat,
        does = R.string.product_ingredient_about_makes_dough_stronger,
        sources = listOf("21 CFR 184.1322"),
    ),
    IngredientDescription(
        ids = listOf("en:cocoa-paste"),
        what = R.string.product_ingredient_about_ground_cocoa_beans,
        does = null,
        sources = listOf("21 CFR 163.111"),
    ),
    IngredientDescription(
        ids = listOf("en:cocoa-butter"),
        what = R.string.product_ingredient_about_fat_cocoa_beans,
        does = null,
        sources = listOf(COCOA_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:cocoa", "en:cocoa-powder"),
        what = R.string.product_ingredient_about_roasted_cocoa_beans_ground,
        does = null,
        sources = listOf(COCOA_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:fat-reduced-cocoa", "en:fat-reduced-cocoa-powder"),
        what = R.string.product_ingredient_about_cocoa_powder_less_cocoa,
        does = null,
        sources = listOf(COCOA_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:skimmed-milk-powder"),
        what = R.string.product_ingredient_about_skimmed_milk_water_removed,
        does = null,
        sources = listOf(DRIED_MILK_LAW, "21 CFR 131.125"),
    ),
    IngredientDescription(
        ids = listOf("en:whole-milk-powder"),
        what = R.string.product_ingredient_about_milk_water_removed,
        does = null,
        sources = listOf(DRIED_MILK_LAW, "21 CFR 131.147"),
    ),
    IngredientDescription(
        ids = listOf("en:milk-powder"),
        what = R.string.product_ingredient_about_milk_water_removed_can,
        does = null,
        sources = listOf(DRIED_MILK_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:milk-proteins"),
        what = R.string.product_ingredient_about_proteins_milk_casein_whey,
        does = null,
        sources = listOf(LABEL_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:casein"),
        what = R.string.product_ingredient_about_protein_milk,
        does = null,
        sources = listOf(CASEIN_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:caseinate", "en:calcium-caseinate", "en:sodium-caseinate"),
        what = R.string.product_ingredient_about_made_milk_protein_casein,
        does = null,
        sources = listOf(CASEIN_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:whey"),
        what = R.string.product_ingredient_about_liquid_left_when_milk,
        does = null,
        sources = listOf(CFR_WHEY),
    ),
    IngredientDescription(
        ids = listOf("en:whey-powder"),
        what = R.string.product_ingredient_about_dried_whey_liquid_left_when_milk_curdles_cheese,
        does = null,
        sources = listOf(CFR_WHEY),
    ),
    IngredientDescription(
        ids = listOf("en:sweet-whey-powder"),
        what = R.string.product_ingredient_about_dried_whey_liquid_left_when_milk_curdles_cheese_sweet,
        does = null,
        sources = listOf(CFR_WHEY),
    ),
    IngredientDescription(
        ids = listOf("en:concentrated-whey-protein", "en:whey-protein"),
        what = R.string.product_ingredient_about_protein_whey_liquid_left,
        does = null,
        sources = listOf("21 CFR 184.1979c"),
    ),
    IngredientDescription(
        ids = listOf("en:lactose"),
        what = R.string.product_ingredient_about_milk_sugar_obtained_whey,
        does = null,
        sources = listOf(DRIED_MILK_LAW, "21 CFR 168.122"),
    ),
    IngredientDescription(
        ids = listOf("en:rennet"),
        what = null,
        does = R.string.product_ingredient_about_used_make_cheese,
        sources = listOf("21 CFR 184.1685"),
    ),
    IngredientDescription(
        ids = listOf("en:flavouring"),
        what = R.string.product_ingredient_about_substances_extracts_give_change,
        does = null,
        sources = listOf(FLAVOURINGS_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:natural-flavouring"),
        what = R.string.product_ingredient_about_flavouring_obtained_plants_animals,
        does = R.string.product_ingredient_about_gives_changes_smell_taste,
        sources = listOf(FLAVOURINGS_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:natural-vanilla-flavouring"),
        what = R.string.product_ingredient_about_natural_flavouring_obtained_vanilla,
        does = R.string.product_ingredient_about_gives_changes_smell_taste,
        sources = listOf("Regulation (EC) No 1334/2008, Article 16(4)"),
    ),
    IngredientDescription(
        ids = listOf("en:smoke-flavouring"),
        what = R.string.product_ingredient_about_flavouring_made_wood_smoke,
        does = R.string.product_ingredient_about_gives_smoky_taste,
        sources = listOf(FLAVOURINGS_LAW, "Regulation (EC) No 2065/2003", "EFSA, topic page \"Smoke flavourings\""),
    ),
    IngredientDescription(
        ids = listOf("en:vanillin"),
        what = null,
        does = R.string.product_ingredient_about_gives_flavour,
        sources = listOf(FLAVOURINGS_LAW),
    ),
    IngredientDescription(
        ids = listOf("en:yeast-extract"),
        what = R.string.product_ingredient_about_made_inside_yeast_cells,
        does = R.string.product_ingredient_about_gives_flavour_or_makes_flavour_stronger,
        sources = listOf("21 CFR 184.1983", FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:concentrated-lemon-juice"),
        what = R.string.product_ingredient_about_lemon_juice_part_water,
        does = null,
        sources = listOf("Directive 2001/112/EC"),
    ),
    IngredientDescription(
        ids = listOf("en:e503", "en:e503i", "en:e503ii"),
        what = null,
        does = R.string.product_ingredient_about_makes_baked_goods_rise,
        sources = listOf(ADDITIVE_SPECIFICATIONS, "21 CFR 184.1135", "21 CFR 184.1137"),
    ),
    IngredientDescription(
        ids = listOf("en:e296"),
        what = null,
        does = R.string.product_ingredient_about_controls_how_acidic_food_gives,
        sources = listOf("21 CFR 184.1069"),
    ),
    IngredientDescription(
        ids = listOf("en:e508"),
        what = R.string.product_ingredient_about_potassium_chloride_mineral_tastes,
        does = R.string.product_ingredient_about_gives_flavour_or_makes_it_stronger,
        sources = listOf("21 CFR 184.1622"),
    ),
    IngredientDescription(
        ids = listOf("en:e336i"),
        what = R.string.product_ingredient_about_cream_tartar_left_over,
        does = R.string.product_ingredient_about_makes_baked_goods_rise_controls,
        sources = listOf("21 CFR 184.1077"),
    ),
    IngredientDescription(
        ids = listOf("en:e501i"),
        what = null,
        does = R.string.product_ingredient_about_controls_how_acidic_food_is,
        sources = listOf("21 CFR 184.1619"),
    ),
    IngredientDescription(
        ids = listOf("en:e262", "en:e262i"),
        what = R.string.product_ingredient_about_made_acetic_acid,
        does = R.string.product_ingredient_about_controls_how_acidic_food_gives,
        sources = listOf("21 CFR 184.1721"),
    ),
    IngredientDescription(
        ids = listOf("en:e262ii"),
        what = R.string.product_ingredient_about_made_acetic_acid,
        does = R.string.product_ingredient_about_helps_food_keep_longer_controls,
        sources = listOf("21 CFR 184.1754"),
    ),
    IngredientDescription(
        ids = listOf("en:e385"),
        what = null,
        does = R.string.product_ingredient_about_helps_food_keep_its,
        sources = listOf("21 CFR 172.120", FDA_TYPES),
    ),
    IngredientDescription(
        ids = listOf("en:e338"),
        what = null,
        does = R.string.product_ingredient_about_controls_how_acidic_food_is,
        sources = listOf(EFSA_PHOSPHATES),
    ),
    IngredientDescription(
        ids = listOf("en:e339", "en:e340", "en:e341", "en:e341iii", "en:e450", "en:e450i", "en:e451", "en:e452"),
        what = null,
        does = R.string.product_ingredient_about_control_how_acidic_food,
        sources = listOf(EFSA_PHOSPHATES),
    ),
    IngredientDescription(
        ids = listOf("en:lactic-ferments"),
        what = R.string.product_ingredient_about_bacteria_make_lactic_acid,
        does = R.string.product_ingredient_about_makes_food_ferment_example,
        sources = listOf(CFR_YOGURT),
    ),
    IngredientDescription(
        ids = listOf("en:microbial-culture"),
        what = R.string.product_ingredient_about_bacteria_yeasts_moulds,
        does = R.string.product_ingredient_about_makes_food_ferment,
        sources = listOf("EFFCA"),
    ),
    IngredientDescription(
        ids = listOf("en:lactobacillus-bulgaricus"),
        what = R.string.product_ingredient_about_bacterium_makes_lactic_acid,
        does = R.string.product_ingredient_about_ferments_milk_into_yoghurt,
        sources = listOf(CFR_YOGURT),
    ),
    IngredientDescription(
        ids = listOf("en:yeast"),
        what = R.string.product_ingredient_about_microbes_can_make_dough,
        does = null,
        sources = listOf(COFALEC, CFR_LEAVENING),
    ),
    IngredientDescription(
        ids = listOf("en:baker-s-yeast"),
        what = R.string.product_ingredient_about_microbes_make_dough_rise,
        does = null,
        sources = listOf(COFALEC, CFR_LEAVENING),
    ),
    IngredientDescription(
        ids = listOf("en:sourdough-starter", "en:natural-sourdough"),
        what = R.string.product_ingredient_about_dough_lactic_bacteria_yeasts,
        does = null,
        sources = listOf("German food guidelines for bread, 1.1.5"),
    ),
    IngredientDescription(
        ids = listOf("en:microbial-rennet"),
        what = null,
        does = R.string.product_ingredient_about_used_make_cheese,
        sources = listOf(EFSA_MUCORPEPSIN),
    ),
    IngredientDescription(
        ids = listOf("en:starch", "en:wheat-starch", "en:corn-starch", "en:potato-starch"),
        what = R.string.product_ingredient_about_eu_labels_count_carbohydrate,
        does = null,
        sources = listOf(LABEL_LAW_ANNEX_XV),
    ),
    IngredientDescription(
        ids = listOf(
            "en:fiber",
            "en:wheat-fiber",
            "en:oat-fibre",
            "en:citrus-fibre",
            "en:pea-fiber",
            "en:vegetable-fiber",
            "en:soluble-fibre",
        ),
        what = R.string.product_ingredient_about_eu_labels_list_fibre,
        does = null,
        sources = listOf(LABEL_LAW_ANNEX_XV),
    ),
    IngredientDescription(
        ids = listOf("en:inulin"),
        what = R.string.product_ingredient_about_fibre_plants,
        does = null,
        sources = listOf(FDA_FIBRE),
    ),
    IngredientDescription(
        ids = listOf("en:chicory-fibre"),
        what = R.string.product_ingredient_about_fibre_chicory,
        does = null,
        sources = listOf(FDA_FIBRE),
    ),
    IngredientDescription(
        ids = listOf("en:psyllium"),
        what = R.string.product_ingredient_about_husk_psyllium_seeds_fibre,
        does = null,
        sources = listOf(FDA_FIBRE, "21 CFR 101.81"),
    ),
    IngredientDescription(
        ids = listOf("en:malted-barley"),
        what = R.string.product_ingredient_about_sprouted_barley,
        does = null,
        sources = listOf(CFR_MALT),
    ),
    IngredientDescription(
        ids = listOf("en:barley-malt-flour"),
        what = R.string.product_ingredient_about_flour_sprouted_barley,
        does = null,
        sources = listOf(CFR_MALT),
    ),
    IngredientDescription(
        ids = listOf("en:caffeine"),
        what = R.string.product_ingredient_about_substance_occurs_naturally_coffee,
        does = null,
        sources = listOf("EFSA topic page on caffeine"),
    ),
    IngredientDescription(
        ids = listOf("en:taurine"),
        what = R.string.product_ingredient_about_substance_added_energy_drinks,
        does = null,
        sources = listOf("EFSA, topic page \"Caffeine\""),
    ),
    IngredientDescription(
        ids = listOf("en:e331"),
        what = R.string.product_ingredient_about_made_citric_acid,
        does = null,
        sources = listOf("21 CFR 184.1751"),
    ),
    IngredientDescription(
        ids = listOf("en:e101", "en:e101i"),
        what = R.string.product_ingredient_about_vitamin_b2,
        does = R.string.product_ingredient_about_colours_food,
        sources = listOf(VITAMINS_LAW_ANNEX_II, UK_LIST),
    ),
)
