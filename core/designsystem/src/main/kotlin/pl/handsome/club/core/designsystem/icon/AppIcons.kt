package pl.handsome.club.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
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
private const val FAVOURITE =
    "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z"
private const val FAVOURITE_BORDER =
    "M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3 4.42,3 2,5.42 2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5 22,5.42 19.58,3 16.5,3zM12.1,18.55l-0.1,0.1 -0.1,-0.1C7.14,14.24 4,11.39 4,8.5 4,6.5 5.5,5 7.5,5c1.54,0 3.04,0.99 3.57,2.36h1.87C13.46,5.99 14.96,5 16.5,5c2,0 3.5,1.5 3.5,3.5 0,2.89 -3.14,5.74 -7.9,10.05z"
private const val MORE =
    "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"

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
    val Favourite: ImageVector by lazy { icon("Favourite", 36.dp, FAVOURITE) }
    val FavouriteBorder: ImageVector by lazy { icon("FavouriteBorder", 36.dp, FAVOURITE_BORDER) }
    val More: ImageVector by lazy { icon("More", 32.dp, MORE) }
}

private fun icon(name: String, size: Dp, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = size,
        defaultHeight = size,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
    }.build()

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
