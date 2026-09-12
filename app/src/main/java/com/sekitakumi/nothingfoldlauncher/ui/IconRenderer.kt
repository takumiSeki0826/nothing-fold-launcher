package com.sekitakumi.nothingfoldlauncher.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private const val ICON_SIZE_PX = 128

/**
 * Nothing風の統一感を出すため、アプリアイコンをグレースケール化し丸型にマスクする。
 * 変換に失敗した場合はドット柄のプレースホルダーを返す。
 */
fun renderMonochromeIcon(drawable: Drawable?): ImageBitmap {
    if (drawable == null) return placeholderIcon()

    return try {
        val source = drawableToBitmap(drawable)
        val grayscale = toGrayscale(source)
        maskCircular(grayscale).asImageBitmap()
    } catch (e: Exception) {
        placeholderIcon()
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
    drawable.draw(canvas)
    return bitmap
}

private fun toGrayscale(source: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
    canvas.drawBitmap(source, 0f, 0f, paint)
    return result
}

private fun maskCircular(source: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())

    canvas.drawOval(rect, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(source, 0f, 0f, paint)
    return result
}

private fun placeholderIcon(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }

    val dotSpacing = ICON_SIZE_PX / 6f
    val dotRadius = dotSpacing / 6f
    for (row in 1..4) {
        for (col in 1..4) {
            canvas.drawCircle(col * dotSpacing, row * dotSpacing, dotRadius, paint)
        }
    }
    return bitmap.asImageBitmap()
}
