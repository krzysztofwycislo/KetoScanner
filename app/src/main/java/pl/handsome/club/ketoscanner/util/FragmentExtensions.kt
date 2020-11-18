package pl.handsome.club.ketoscanner.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

fun Fragment.safeNavigateTo(directions: NavDirections) = with(findNavController()) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}