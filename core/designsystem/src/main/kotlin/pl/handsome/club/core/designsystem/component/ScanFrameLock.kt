package pl.handsome.club.core.designsystem.component

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

fun scanFrameLockOf(target: Rect, frame: Rect): Rect? {
    if (frame.isEmpty || !target.overlaps(frame)) return null
    return Rect(
        left = ((target.left - frame.left) / frame.width).coerceIn(0f, 1f),
        top = ((target.top - frame.top) / frame.height).coerceIn(0f, 1f),
        right = ((target.right - frame.left) / frame.width).coerceIn(0f, 1f),
        bottom = ((target.bottom - frame.top) / frame.height).coerceIn(0f, 1f),
    )
}

fun scanFrameBracketTarget(size: Size, inset: Float, lock: Rect?, lockPadding: Float): Rect {
    val bounds = Rect(inset, inset, size.width - inset, size.height - inset)
    if (lock == null) return bounds
    return Rect(
        left = lock.left * size.width,
        top = lock.top * size.height,
        right = lock.right * size.width,
        bottom = lock.bottom * size.height,
    ).inflate(lockPadding).intersect(bounds)
}
