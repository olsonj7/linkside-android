package com.linkside.app.ui.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

object ShareLinks {
    /** Same install URL baked into iOS share cards (App Store). */
    const val INSTALL_URL = "https://apps.apple.com/us/app/linkside-golf/id6766528004"
}

fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bmp.setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bmp
}

fun cacheShareBitmap(context: Context, bitmap: Bitmap, fileName: String = "linkside_share.png"): Uri {
    val dir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(dir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun shareImageAndText(context: Context, imageUri: Uri, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newUri(context.contentResolver, "Linkside", imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share round"))
}

/**
 * Shares a background image to Instagram Stories when the app is installed.
 * Returns false if Instagram isn't available.
 */
fun shareToInstagramStory(context: Context, imageUri: Uri): Boolean {
    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setDataAndType(imageUri, "image/png")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setPackage("com.instagram.android")
        putExtra("source_application", context.packageName)
    }
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
