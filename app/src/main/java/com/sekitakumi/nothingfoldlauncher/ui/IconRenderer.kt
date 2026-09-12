package com.sekitakumi.nothingfoldlauncher.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private const val ICON_SIZE_PX = 128
private const val HALFTONE_GRID = 8

/**
 * Nothing風のハーフトーン(ドット絵)処理でアプリアイコンを描画する。
 * 変換に失敗した場合はドット柄のプレースホルダーを返す。
 */
fun renderHalftoneIcon(drawable: Drawable?): ImageBitmap {
    if (drawable == null) return placeholderIcon()

    return try {
        val source = drawableToBitmap(drawable)
        halftone(source).asImageBitmap()
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

private fun halftone(source: Bitmap): Bitmap {
    val small = Bitmap.createScaledBitmap(source, HALFTONE_GRID, HALFTONE_GRID, true)

    val result = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    canvas.drawColor(Color.BLACK)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    val cellSize = ICON_SIZE_PX.toFloat() / HALFTONE_GRID

    for (row in 0 until HALFTONE_GRID) {
        for (col in 0 until HALFTONE_GRID) {
            val pixel = small.getPixel(col, row)
            val luminance = luminanceOf(pixel)
            val radius = (cellSize / 2f) * dotRadiusRatio(luminance)
            canvas.drawCircle(
                col * cellSize + cellSize / 2f,
                row * cellSize + cellSize / 2f,
                radius,
                paint,
            )
        }
    }
    return result
}

private fun luminanceOf(pixel: Int): Float {
    val alpha = Color.alpha(pixel) / 255f
    val r = Color.red(pixel) / 255f
    val g = Color.green(pixel) / 255f
    val b = Color.blue(pixel) / 255f
    // 透明部分は輝度0(=最小ドット)として扱う
    return alpha * (0.299f * r + 0.587f * g + 0.114f * b)
}

private fun placeholderIcon(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.BLACK)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    val dotSpacing = ICON_SIZE_PX / 6f
    val dotRadius = dotSpacing / 6f
    for (row in 1..4) {
        for (col in 1..4) {
            canvas.drawCircle(col * dotSpacing, row * dotSpacing, dotRadius, paint)
        }
    }
    return bitmap.asImageBitmap()
}
