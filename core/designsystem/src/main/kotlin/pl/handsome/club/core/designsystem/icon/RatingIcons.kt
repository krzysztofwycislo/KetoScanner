package pl.handsome.club.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val LEFT_EYE = "M8.5,9.5m-1.5,0a1.5,1.5 0,1 1,3 0a1.5,1.5 0,1 1,-3 0"
private const val RIGHT_EYE = "M15.5,9.5m-1.5,0a1.5,1.5 0,1 1,3 0a1.5,1.5 0,1 1,-3 0"
private const val FACE_OUTLINE =
    "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8z"
private const val HAPPY_MOUTH =
    "M12,17.5c2.33,0 4.32,-1.45 5.12,-3.5h-1.67c-0.69,1.19 -1.97,2 -3.45,2s-2.75,-0.81 -3.45,-2L6.88,14c0.8,2.05 2.79,3.5 5.12,3.5z"
private const val NEUTRAL_MOUTH =
    "M12,16c-0.73,0 -1.38,-0.18 -1.96,-0.52 -0.12,0.14 -0.86,0.98 -1.01,1.15 0.86,0.55 1.87,0.87 2.97,0.87 1.11,0 2.12,-0.33 2.98,-0.88 -0.97,-1.09 -0.01,-0.02 -1.01,-1.15 -0.59,0.35 -1.24,0.53 -1.97,0.53z"
private const val SAD_MOUTH =
    "M12,14c-2.33,0 -4.32,1.45 -5.12,3.5h1.67c0.69,-1.19 1.97,-2 3.45,-2s2.75,0.81 3.45,2h1.67c-0.8,-2.05 -2.79,-3.5 -5.12,-3.5z"
private const val QUESTION =
    "M11,18h2v-2h-2v2zM12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12,6c-2.21,0 -4,1.79 -4,4h2c0,-1.1 0.9,-2 2,-2s2,0.9 2,2c0,2 -3,1.75 -3,5h2c0,-2.25 3,-2.5 3,-5 0,-2.21 -1.79,-4 -4,-4z"
private const val PORTION =
    "M12,2C8.43,2 5.23,3.54 3.01,6L12,22l8.99,-16C18.78,3.55 15.57,2 12,2zM7,7c0,-1.1 0.9,-2 2,-2s2,0.9 2,2 -0.9,2 -2,2 -2,-0.9 -2,-2zM12,15c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z"

internal object RatingIcons {
    val Happy: ImageVector by lazy { icon("RatingHappy", LEFT_EYE, RIGHT_EYE, FACE_OUTLINE + HAPPY_MOUTH) }
    val Neutral: ImageVector by lazy { icon("RatingNeutral", LEFT_EYE, RIGHT_EYE, FACE_OUTLINE + NEUTRAL_MOUTH) }
    val Sad: ImageVector by lazy { icon("RatingSad", LEFT_EYE, RIGHT_EYE, FACE_OUTLINE + SAD_MOUTH) }
    val Question: ImageVector by lazy { icon("RatingQuestion", QUESTION) }
    val Portion: ImageVector by lazy { icon("RatingPortion", PORTION) }
}

private fun icon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    }.build()
