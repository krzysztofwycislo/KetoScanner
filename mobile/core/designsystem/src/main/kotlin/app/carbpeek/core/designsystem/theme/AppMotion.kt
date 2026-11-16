package app.carbpeek.core.designsystem.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MotionSpring(val dampingRatio: Float, val stiffness: Float)

fun <T> MotionSpring.spec(visibilityThreshold: T? = null): SpringSpec<T> =
    spring(dampingRatio = dampingRatio, stiffness = stiffness, visibilityThreshold = visibilityThreshold)

@Immutable
data class AppMotion(
    val durationShort: Int,
    val durationMedium: Int,
    val easingStandard: Easing,
    val springSettle: MotionSpring,
    val springPop: MotionSpring,
    val popFromScale: Float,
    val aimSweepMillis: Int,
    val shakeMillis: Int,
    val shakeDistance: Dp,
    val dismissFraction: Float,
    val dismissVelocity: Dp,
)

val DefaultAppMotion = AppMotion(
    durationShort = 150,
    durationMedium = 200,
    easingStandard = LinearOutSlowInEasing,
    springSettle = MotionSpring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
    springPop = MotionSpring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
    popFromScale = 0.6f,
    aimSweepMillis = 2400,
    shakeMillis = 320,
    shakeDistance = 8.dp,
    dismissFraction = 0.25f,
    dismissVelocity = 800.dp,
)
