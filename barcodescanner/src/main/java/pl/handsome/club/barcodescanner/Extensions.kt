package pl.handsome.club.barcodescanner

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.camera.core.ImageProxy

// Code from https://github.com/googlecodelabs/mlkit-android

private val CHANNEL_RANGE = 0 until (1 shl 18)

fun ImageProxy.convertYuv420888ImageToBitmap(): Bitmap {
    require(format == ImageFormat.YUV_420_888) {
        "Unsupported image format $(image.format)"
    }

    val planes = planes

    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    val yuvBytes = planes.map { plane ->
        val buffer = plane.buffer
        val yuvBytes = ByteArray(buffer.capacity())
        buffer[yuvBytes]
        buffer.rewind()  // Be kind…
        yuvBytes
    }

    val yRowStride = planes[0].rowStride
    val uvRowStride = planes[1].rowStride
    val uvPixelStride = planes[1].pixelStride
    val width = width
    val height = height
    @ColorInt val argb8888 = IntArray(width * height)
    var i = 0
    for (y in 0 until height) {
        val pY = yRowStride * y
        val uvRowStart = uvRowStride * (y shr 1)
        for (x in 0 until width) {
            val uvOffset = (x shr 1) * uvPixelStride
            argb8888[i++] =
                yuvToRgb(
                    yuvBytes[0][pY + x].toIntUnsigned(),
                    yuvBytes[1][uvRowStart + uvOffset].toIntUnsigned(),
                    yuvBytes[2][uvRowStart + uvOffset].toIntUnsigned()
                )
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(argb8888, 0, width, 0, 0, width, height)
    return bitmap
}

@ColorInt
private fun yuvToRgb(nY: Int, nU: Int, nV: Int): Int {
    var nY = nY
    var nU = nU
    var nV = nV
    nY -= 16
    nU -= 128
    nV -= 128
    nY = nY.coerceAtLeast(0)

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);
    var nR = 1192 * nY + 1634 * nV
    var nG = 1192 * nY - 833 * nV - 400 * nU
    var nB = 1192 * nY + 2066 * nU

    // Clamp the values before normalizing them to 8 bits.
    nR = nR.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
    nG = nG.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
    nB = nB.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
    return -0x1000000 or (nR shl 16) or (nG shl 8) or nB
}


private fun Byte.toIntUnsigned(): Int {
    return toInt() and 0xFF
}

fun Bitmap.rotateAndCrop(
    rotationDegrees: Int,
    cropRect: Rect
): Bitmap {
    val matrix = Matrix()
    matrix.preRotate(rotationDegrees.toFloat())

    return Bitmap.createBitmap(
        this,
        cropRect.left,
        cropRect.top,
        cropRect.width(),
        cropRect.height(),
        matrix,
        true
    )
}