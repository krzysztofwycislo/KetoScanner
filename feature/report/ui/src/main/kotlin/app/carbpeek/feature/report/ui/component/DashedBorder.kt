package app.carbpeek.feature.report.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Dp

internal fun Modifier.dashedBorder(width: Dp, color: Color, shape: Shape, dash: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    inset(stroke / 2) {
        drawOutline(
            outline = shape.createOutline(size = size, layoutDirection = layoutDirection, density = this),
            color = color,
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dash.toPx(), dash.toPx()),
                ),
            ),
        )
    }
}
