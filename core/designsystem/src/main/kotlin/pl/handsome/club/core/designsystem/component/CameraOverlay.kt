package pl.handsome.club.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.designsystem.theme.spec
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private val SCAN_FRAME_STROKE = 5.dp
private val SCAN_FRAME_INSET = 3.dp
private val SCAN_FRAME_RADIUS = 13.dp
private val SCAN_FRAME_ARM = 37.dp

enum class ScanFrameMode { Aiming, Idle, Searching, Found, NotFound }

private val ScanFrameMode.usesLock: Boolean
    get() = this == ScanFrameMode.Searching || this == ScanFrameMode.Found || this == ScanFrameMode.NotFound

@Composable
fun Ui.ScanFrame(mode: ScanFrameMode, lock: Rect?, modifier: Modifier = Modifier) {
    val motion = AppTheme.motion
    val density = LocalDensity.current
    val frameWidth = AppTheme.sizes.scanFrameWidth
    val frameHeight = AppTheme.sizes.scanFrameHeight
    val lockPadding = AppTheme.spacing.s
    val strokeColor by animateColorAsState(
        targetValue = if (mode == ScanFrameMode.Found) AppTheme.colors.text.highlight else AppTheme.colors.text.inverse,
        animationSpec = tween(motion.durationShort),
        label = "ScanFrameColor",
    )
    val shake = rememberShakeOffset(shaking = mode == ScanFrameMode.NotFound)
    BoxWithConstraints(
        modifier = modifier
            .sizeIn(maxWidth = frameWidth, maxHeight = frameHeight)
            .aspectRatio(frameWidth / frameHeight),
        contentAlignment = Alignment.Center,
    ) {
        val scale = min(maxWidth / frameWidth, maxHeight / frameHeight)
        val aimTravel = (maxHeight / 2 - AppTheme.spacing.xl).coerceAtLeast(0.dp)
        val bracketTarget = with(density) {
            scanFrameBracketTarget(
                size = Size(maxWidth.toPx(), maxHeight.toPx()),
                inset = (SCAN_FRAME_INSET * scale).toPx(),
                lock = lock.takeIf { mode.usesLock },
                lockPadding = lockPadding.toPx(),
            )
        }
        val bracket by animateRectAsState(
            targetValue = bracketTarget,
            animationSpec = motion.springSettle.spec(Rect.VisibilityThreshold),
            label = "ScanFrameBracket",
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            translate(left = shake.value) {
                drawPath(
                    path = scanFrameBrackets(
                        bracket = bracket,
                        maxArm = (SCAN_FRAME_ARM * scale).toPx(),
                        maxRadius = (SCAN_FRAME_RADIUS * scale).toPx(),
                    ),
                    color = strokeColor,
                    style = Stroke(width = (SCAN_FRAME_STROKE * scale).toPx(), cap = StrokeCap.Round),
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationX = shake.value },
            contentAlignment = Alignment.Center,
        ) {
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

private fun scanFrameBrackets(bracket: Rect, maxArm: Float, maxRadius: Float): Path {
    val arm = minOf(maxArm, bracket.width / 3, bracket.height / 3)
    val radius = minOf(maxRadius, arm)
    val corner = Size(radius * 2, radius * 2)
    return Path().apply {
        with(bracket) {
            moveTo(left, top + arm)
            lineTo(left, top + radius)
            arcTo(Rect(Offset(left, top), corner), 180f, 90f, false)
            lineTo(left + arm, top)

            moveTo(right - arm, top)
            lineTo(right - radius, top)
            arcTo(Rect(Offset(right - corner.width, top), corner), 270f, 90f, false)
            lineTo(right, top + arm)

            moveTo(right, bottom - arm)
            lineTo(right, bottom - radius)
            arcTo(Rect(Offset(right - corner.width, bottom - corner.height), corner), 0f, 90f, false)
            lineTo(right - arm, bottom)

            moveTo(left + arm, bottom)
            lineTo(left + radius, bottom)
            arcTo(Rect(Offset(left, bottom - corner.height), corner), 90f, 90f, false)
            lineTo(left, bottom - arm)
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
            .height(AppTheme.sizes.aimLine)
            .background(AppTheme.colors.text.highlight, AppTheme.shapes.pill),
    )
}

@Composable
private fun SearchingDisc() {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.messageIcon)
            .background(AppTheme.colors.camera.hint, AppTheme.shapes.circle),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = AppTheme.colors.text.highlight,
            trackColor = Color.Transparent,
            modifier = Modifier.size(AppTheme.sizes.iconM),
        )
    }
}

@Composable
private fun FoundDisc() {
    Ui.PopIn {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.scanCheck)
                .background(AppTheme.colors.text.highlight, AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Check.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.default,
                modifier = Modifier.size(AppTheme.sizes.iconM),
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
            .size(AppTheme.sizes.touchTarget)
            .clip(AppTheme.shapes.circle)
            .background(if (active) colors.camera.controlActive else colors.camera.control)
            .toggleable(value = active, role = Role.Switch, onValueChange = { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription.resolve(),
            tint = if (active) colors.text.default else colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.iconS),
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
            .height(AppTheme.sizes.touchTarget)
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
            modifier = Modifier.size(AppTheme.sizes.iconS),
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

private val PreviewLock = Rect(0.2f, 0.35f, 0.8f, 0.65f)

@Preview
@Composable
private fun ScanFrameAimingPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Aiming, lock = null)
    }
}

@Preview
@Composable
private fun ScanFrameIdlePreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Idle, lock = null)
    }
}

@Preview
@Composable
private fun ScanFrameSearchingPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Searching, lock = PreviewLock)
    }
}

@Preview
@Composable
private fun ScanFrameScaledDownPreview() {
    CameraPreviewBox {
        Box(Modifier.size(200.dp, 150.dp)) {
            Ui.ScanFrame(mode = ScanFrameMode.Searching, lock = PreviewLock)
        }
    }
}

@Preview
@Composable
private fun ScanFrameFoundPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Found, lock = PreviewLock)
    }
}

@Preview
@Composable
private fun ScanFrameFoundWithoutLockPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.Found, lock = null)
    }
}

@Preview
@Composable
private fun ScanFrameNotFoundPreview() {
    CameraPreviewBox {
        Ui.ScanFrame(mode = ScanFrameMode.NotFound, lock = PreviewLock)
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
