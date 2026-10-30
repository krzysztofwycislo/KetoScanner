package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme

private const val SCAN_FRAME_WIDTH = 320f
private const val SCAN_FRAME_HEIGHT = 200f
private const val SCAN_FRAME =
    "M3 40V16A13 13 0 0 1 16 3h24M280 3h24a13 13 0 0 1 13 13v24M317 160v24a13 13 0 0 1-13 13h-24M40 197H16A13 13 0 0 1 3 184v-24"

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

@Composable
fun Ui.ScanFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(AppTheme.sizes.scanFrameWidth, AppTheme.sizes.scanFrameHeight),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = ScanFrameVector,
            contentDescription = null,
            colorFilter = ColorFilter.tint(AppTheme.colors.text.inverse),
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.xxl)
                .height(AppTheme.sizes.aimLine)
                .background(AppTheme.colors.text.highlight, AppTheme.shapes.pill),
        )
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
private fun ScanFramePreview() {
    CameraPreviewBox {
        Ui.ScanFrame()
    }
}

@Preview
@Composable
private fun CameraIconButtonPreview() {
    CameraPreviewBox {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            val description = UiText.Res(R.string.designsystem_preview_button)
            Ui.CameraIconButton(AppIcons.Torch, description, active = false, onClick = {})
            Ui.CameraIconButton(AppIcons.Torch, description, active = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun CameraPillButtonAndHintPreview() {
    CameraPreviewBox {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            val text = stringResource(R.string.designsystem_preview_button)
            Ui.CameraPillButton(text = text, icon = AppIcons.Keyboard, onClick = {})
            Ui.CameraHint(text = text)
        }
    }
}
