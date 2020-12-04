package pl.handsome.club.ketoscanner.preferences

interface SharedPreferenceMediator {

  fun isOnKeto(): Boolean
  fun maxCarbsAmount(): Int
  fun setMaxCarbsAmount(newValue: Int)
  fun setIsOnKeto(newValue: Boolean)

  companion object {
    const val DEFAULT_PREFERENCES_KEY = "KetoSPreferences"

    const val IS_ON_KETO_PREF = "isOnKeto"
    const val MAX_CARBS_AMOUNT = "carbAmount"
  }

}
