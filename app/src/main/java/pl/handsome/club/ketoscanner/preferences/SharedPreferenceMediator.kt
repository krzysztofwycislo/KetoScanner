package pl.handsome.club.ketoscanner.preferences

interface SharedPreferenceMediator {

  fun maxCarbsAmount(): Int
  fun setMaxCarbsAmount(newValue: Int)


  companion object {

    const val DEFAULT_PREFERENCES_KEY = "KetoSPreferences"

    const val MAX_CARBS_AMOUNT = "carbAmount"

  }

}
