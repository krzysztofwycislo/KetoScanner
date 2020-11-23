package pl.handsome.club.ketoscanner.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import pl.handsome.club.ketoscanner.R


val defaultAnimationOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.nav_enter_anim)
    .setExitAnim(R.anim.nav_exit_anim)
    .setPopEnterAnim(R.anim.nav_pop_enter_anim)
    .setPopExitAnim(R.anim.nav_pop_exit_anim)
    .build()

fun Fragment.safeNavigateTo(directions: NavDirections) = with(findNavController()) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions, defaultAnimationOptions) }
}