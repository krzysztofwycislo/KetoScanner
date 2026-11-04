package pl.handsome.club.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.designsystem.theme.spec

private val ShakePattern = listOf(1f, -1f, 1f, -1f)

@Composable
fun <T : Any> Ui.FadeVisibility(
    value: T?,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    var lastValue by remember { mutableStateOf(value) }
    if (value != null) lastValue = value
    AnimatedVisibility(
        visible = value != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        lastValue?.let { content(it) }
    }
}

@Composable
fun <T> Ui.FadeSwap(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val motion = AppTheme.motion
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            val fade = fadeIn(tween(motion.durationShort)) togetherWith fadeOut(tween(motion.durationShort))
            fade using SizeTransform(clip = false)
        },
        label = "FadeSwap",
    ) { state ->
        content(state)
    }
}

@Composable
fun Ui.PopIn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val motion = AppTheme.motion
    val scale = remember { Animatable(motion.popFromScale) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, motion.springPop.spec())
    }
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    ) {
        content()
    }
}

@Composable
fun rememberShakeOffset(shaking: Boolean): State<Float> {
    val motion = AppTheme.motion
    val density = LocalDensity.current
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shaking) {
        if (shaking) {
            val distance = with(density) { motion.shakeDistance.toPx() }
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = motion.shakeMillis
                    ShakePattern.forEachIndexed { index, direction ->
                        distance * direction at motion.shakeMillis * (index + 1) / (ShakePattern.size + 1)
                    }
                },
            )
        } else {
            shake.snapTo(0f)
        }
    }
    return shake.asState()
}

@Composable
private fun PreviewLabel(text: UiText) {
    Text(
        text = text.resolve(),
        style = AppTheme.typography.body,
        color = AppTheme.colors.text.default,
        modifier = Modifier
            .background(AppTheme.colors.background.surface)
            .padding(AppTheme.spacing.m),
    )
}

@Preview
@Composable
private fun FadeVisibilityPreview() {
    AppTheme {
        Ui.FadeVisibility(value = UiText.Res(R.string.designsystem_preview_body)) {
            PreviewLabel(it)
        }
    }
}

@Preview
@Composable
private fun FadeSwapPreview() {
    AppTheme {
        Ui.FadeSwap(targetState = UiText.Res(R.string.designsystem_preview_body)) {
            PreviewLabel(it)
        }
    }
}

@Preview
@Composable
private fun PopInPreview() {
    AppTheme {
        Ui.PopIn {
            PreviewLabel(UiText.Res(R.string.designsystem_preview_body))
        }
    }
}
