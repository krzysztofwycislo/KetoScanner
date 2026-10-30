package pl.handsome.club.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val BACK = "M15 5l-7 7 7 7"
private const val HOME = "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"
private const val SCAN =
    "M3 7V5a2 2 0 0 1 2-2h2M17 3h2a2 2 0 0 1 2 2v2M21 17v2a2 2 0 0 1-2 2h-2M7 21H5a2 2 0 0 1-2-2v-2M7 8v8M10 8v8M14 8v8M17 8v8"
private const val HEART =
    "M12 20s-7-4.4-9.3-8.6C1.2 8.6 2.9 5 6.4 5c2 0 3.2 1 4 2.2.4.6.8.6 1.2 0C12.4 6 13.6 5 15.6 5c3.5 0 5.2 3.6 3.7 6.4C19 15.6 12 20 12 20z"
private const val CHECK = "M5 12.5 10 17l9-10"
private const val MINUS = "M6 12h12"
private const val CROSS = "M7 7l10 10M17 7 7 17"
private const val QUESTION = "M9.2 9.2a2.8 2.8 0 1 1 3.9 2.6c-.7.3-1.1.9-1.1 1.6v.6M12 17.5h.01"
private const val CHEVRON_RIGHT = "M9 6l6 6-6 6"
private const val TORCH = "M8 3h8l-1 6H9zM9 9h6v3.5L13.5 21h-3L9 12.5z"
private const val KEYBOARD =
    "M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zM6 10h.01M10 10h.01M14 10h.01M18 10h.01M7 14h10"
private const val CAMERA_OFF =
    "M3 3l18 18M9.5 5h5l1.5 2h4a1 1 0 0 1 1 1v9.5M17 19H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1h3M10 10.6a3 3 0 0 0 4 4"
private const val BAG = "M5 8h14l-1 13H6zM9 8V6a3 3 0 0 1 6 0v2"
private const val LOGO = "M5 5v14M9 5v14M13 5v14M16 5v14M19 5v14"
private const val SLIDERS =
    "M4 7h10M18 7h2M4 17h4M12 17h8M14 7a2 2 0 1 0 4 0a2 2 0 1 0-4 0M8 17a2 2 0 1 0 4 0a2 2 0 1 0-4 0"
private const val INFO = "M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0M12 11v5M12 8h.01"

object AppIcons {
    val Back: ImageVector by lazy { strokeIcon("Back", BACK, strokeWidth = 2f) }
    val Home: ImageVector by lazy { strokeIcon("Home", HOME, strokeWidth = 2f) }
    val Scan: ImageVector by lazy { strokeIcon("Scan", SCAN, strokeWidth = 2.2f) }
    val Heart: ImageVector by lazy { strokeIcon("Heart", HEART, strokeWidth = 2f) }
    val HeartFilled: ImageVector by lazy { strokeIcon("HeartFilled", HEART, strokeWidth = 2f, filled = true) }
    val Check: ImageVector by lazy { strokeIcon("Check", CHECK, strokeWidth = 3f) }
    val Minus: ImageVector by lazy { strokeIcon("Minus", MINUS, strokeWidth = 3f) }
    val Cross: ImageVector by lazy { strokeIcon("Cross", CROSS, strokeWidth = 3f) }
    val Question: ImageVector by lazy { strokeIcon("Question", QUESTION, strokeWidth = 3f) }
    val ChevronRight: ImageVector by lazy { strokeIcon("ChevronRight", CHEVRON_RIGHT, strokeWidth = 2.4f) }
    val Torch: ImageVector by lazy { strokeIcon("Torch", TORCH, strokeWidth = 2f) }
    val Keyboard: ImageVector by lazy { strokeIcon("Keyboard", KEYBOARD, strokeWidth = 2f) }
    val CameraOff: ImageVector by lazy { strokeIcon("CameraOff", CAMERA_OFF, strokeWidth = 2f) }
    val Bag: ImageVector by lazy { strokeIcon("Bag", BAG, strokeWidth = 1.8f) }
    val Logo: ImageVector by lazy { strokeIcon("Logo", LOGO, strokeWidth = 2.4f) }
    val Sliders: ImageVector by lazy { strokeIcon("Sliders", SLIDERS, strokeWidth = 2f) }
    val Info: ImageVector by lazy { strokeIcon("Info", INFO, strokeWidth = 2f) }
}

private fun strokeIcon(name: String, pathData: String, strokeWidth: Float, filled: Boolean = false): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = addPathNodes(pathData),
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
