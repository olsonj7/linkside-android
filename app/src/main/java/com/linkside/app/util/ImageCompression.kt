package com.linkside.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageCompression {
    /**
     * Downscale to max 640px on the long edge and JPEG-encode at ~0.75 quality,
     * matching iOS AccountView.compressImageData.
     */
    fun compressForAvatar(rawBytes: ByteArray, maxDimension: Int = 640, quality: Int = 75): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val longest = max(bounds.outWidth, bounds.outHeight)
        val sample = max(1, longest / maxDimension)
        val decode = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decode) ?: return null

        val scale = minOf(1f, maxDimension.toFloat() / max(bitmap.width, bitmap.height))
        val scaled = if (scale < 1f) {
            val w = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
            val h = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, w, h, true).also {
                if (it !== bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }

        return try {
            ByteArrayOutputStream().use { out ->
                if (!scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)) return null
                out.toByteArray()
            }
        } finally {
            scaled.recycle()
        }
    }
}
