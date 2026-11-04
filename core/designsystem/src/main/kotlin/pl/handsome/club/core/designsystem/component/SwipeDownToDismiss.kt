package pl.handsome.club.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.designsystem.theme.spec

@Composable
fun Ui.SwipeDownToDismiss(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val motion = AppTheme.motion
    val dismissVelocity = with(LocalDensity.current) { motion.dismissVelocity.toPx() }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var height by remember { mutableIntStateOf(0) }
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val springBack = motion.springSettle.spec<Float>()
    LaunchedEffect(visible) {
        if (visible) {
            offset.animateTo(0f, springBack)
        }
    }
    val settle: suspend (Float) -> Unit = { velocity ->
        offset.settle(
            velocity = velocity,
            dismissDistance = height * motion.dismissFraction,
            dismissVelocity = dismissVelocity,
            springBack = springBack,
            onDismiss = currentOnDismiss,
        )
    }
    val currentSettle by rememberUpdatedState(settle)
    val nestedScrollConnection = remember(offset, scope) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y >= 0f || offset.value <= 0f) {
                    return Offset.Zero
                }
                val consumed = available.y.coerceAtLeast(-offset.value)
                scope.launch { offset.snapTo((offset.value + consumed).coerceAtLeast(0f)) }
                return Offset(0f, consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f) {
                    return Offset.Zero
                }
                scope.launch { offset.snapTo(offset.value + available.y) }
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offset.value <= 0f) {
                    return Velocity.Zero
                }
                currentSettle(available.y)
                return available
            }
        }
    }
    Box(
        modifier = modifier
            .onSizeChanged { height = it.height }
            .graphicsLayer { translationY = offset.value }
            .nestedScroll(nestedScrollConnection)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    scope.launch { offset.snapTo((offset.value + delta).coerceAtLeast(0f)) }
                },
                onDragStopped = { velocity -> settle(velocity) },
            ),
    ) {
        content()
    }
}

private suspend fun Animatable<Float, AnimationVector1D>.settle(
    velocity: Float,
    dismissDistance: Float,
    dismissVelocity: Float,
    springBack: AnimationSpec<Float>,
    onDismiss: () -> Unit,
) {
    if (value > dismissDistance || velocity > dismissVelocity) {
        onDismiss()
    } else {
        animateTo(0f, springBack)
    }
}
