package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import kotlin.math.PI
import kotlin.math.sin

private const val SCAN_FRAME_WIDTH = 320f
private const val SCAN_FRAME_HEIGHT = 240f
private const val SCAN_FRAME =
    "M3 40V16A13 13 0 0 1 16 3h24M280 3h24a13 13 0 0 1 13 13v24" +
        "M317 200v24a13 13 0 0 1-13 13h-24M40 237H16A13 13 0 0 1 3 224v-24"

private val ScanFrameVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "ScanFrame",
        defaultWidth = SCAN_FRAME_WIDTH.dp,
        defaultHeight = SCAN_FRAME_HEIGHT.dp,
        viewportWidth = SCAN_FRAME_WIDTH,
        viewportHeight = SCAN_FRAME_HEIGHT,
    ).apply {
        addPath(
            pathData = addPathNodes(SCAN_FRAME),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 5f,
            strokeLineCap = StrokeCap.Round,
        )
    }.build()
}

enum class ScanFrameMode { Aiming, Idle, Searching, Found, NotFound }

@Composable
fun Ui.ScanFrame(mode: ScanFrameMode, modifier: Modifier = Modifier) {
    val motion = AppTheme.motion
    val density = LocalDensity.current
    val strokeColor by animateColorAsState(
        targetValue = if (mode == ScanFrameMode.Found) AppTheme.colors.text.highlight else AppTheme.colors.text.inverse,
        animationSpec = tween(motion.durationShort),
        label = "ScanFrameColor",
    )
    val shake = rememberShakeOffset(shaking = mode == ScanFrameMode.NotFound)
    BoxWithConstraints(
        modifier = modifier
            .sizeIn(maxWidth = SCAN_FRAME_WIDTH.dp, maxHeight = SCAN_FRAME_HEIGHT.dp)
            .aspectRatio(SCAN_FRAME_WIDTH / SCAN_FRAME_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        val aimTravel = (maxHeight / 2 - AppTheme.spacing.xl).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationX = shake.value },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = ScanFrameVector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(strokeColor),
                modifier = Modifier.matchParentSize(),
            )
            when (mode) {
                ScanFrameMode.Aiming -> {
                    SweepingAimLine(travel = with(density) { aimTravel.toPx() })
                }

                ScanFrameMode.Idle -> {
                    AimLine(Modifier)
                }

                ScanFrameMode.Searching, ScanFrameMode.Found, ScanFrameMode.NotFound -> Unit
            }
            Ui.FadeSwap(targetState = mode) { centre ->
                when (centre) {
                    ScanFrameMode.Searching -> {
                        SearchingDisc()
                    }

                    ScanFrameMode.Found -> {
                        FoundDisc()
                    }

                    ScanFrameMode.Aiming, ScanFrameMode.Idle, ScanFrameMode.NotFound -> Unit
                }
            }
        }
    }
}

@Composable
private fun SweepingAimLine(travel: Float) {
    val transition = rememberInfiniteTransition(label = "AimSweep")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AppTheme.motion.aimSweepMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "AimSweepPhase",
    )
    AimLine(Modifier.graphicsLayer { translationY = sin(2 * PI * phase.value).toFloat() * travel })
}

@Composable
private fun AimLine(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.xxl)
            .height(AppTheme.spacing.xxs)
            .background(AppTheme.colors.text.highlight, AppTheme.shapes.pill),
    )
}

@Composable
private fun SearchingDisc() {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.iconDisc)
            .background(AppTheme.colors.camera.hint, AppTheme.shapes.circle),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = AppTheme.colors.text.highlight,
            trackColor = Color.Transparent,
            modifier = Modifier.size(AppTheme.sizes.icon.xl),
        )
    }
}

@Composable
private fun FoundDisc() {
    Ui.PopIn {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.control.l)
                .background(AppTheme.colors.text.highlight, AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Check.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.default,
                modifier = Modifier.size(AppTheme.sizes.icon.xl),
            )
        }
    }
}

@Composable
fun Ui.CameraIconButton(
    icon: ImageVector,
    contentDescription: UiText,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .size(AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.circle)
            .background(if (active) colors.camera.controlActive else colors.camera.control)
            .toggleable(value = active, role = Role.Switch, onValueChange = { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription.resolve(),
            tint = if (active) colors.text.default else colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.icon.m),
        )
    }
}

@Composable
fun Ui.CameraPillButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.pill)
            .background(AppTheme.colors.camera.control)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.icon.m),
        )
        Text(
            text = text,
            style = AppTheme.typography.bodyStrong,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text.inverse,
        )
    }
}

@Composable
fun Ui.CameraHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.text.inverse,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(AppTheme.colors.camera.hint, AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.xs),
    )
}

@Composable
private fun CameraPreviewBox(content: @Composable () -> Unit) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.camera.background)
                .padding(AppTheme.spacing.m),
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun ScanFrameAimingPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Aiming)
    }
}

@Preview
@Composable
private fun ScanFrameIdlePreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Idle)
    }
}

@Preview
@Composable
private fun ScanFrameSearchingPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Searching)
    }
}

@Preview
@Composable
private fun ScanFrameScaledDownPreview() {
    CameraPreviewBox {
        Box(Modifier.size(200.dp, 150.dp)) {
            Ui.ScanFrame(mode = ScanFrameMode.Searching)
        }
    }
}

@Preview
@Composable
private fun ScanFrameFoundPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Found)
    }
}

@Preview
@Composable
private fun ScanFrameNotFoundPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.NotFound)
    }
}

@Preview
@Composable
private fun CameraIconButtonPreview() {
    CameraPreviewBox {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            val description = UiText.Res(R.string.designsystem_preview_button)
            Ui.CameraIconButton(AppIcons.Torch.imageVector, description, active = false, onClick = {})
            Ui.CameraIconButton(AppIcons.Torch.imageVector, description, active = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun CameraPillButtonAndHintPreview() {
    CameraPreviewBox {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            val text = stringResource(R.string.designsystem_preview_button)
            Ui.CameraPillButton(text = text, icon = AppIcons.Keyboard.imageVector, onClick = {})
            Ui.CameraHint(text = text)
        }
    }
}
