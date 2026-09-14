package com.sekitakumi.nothingfoldlauncher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import java.util.Date

/**
 * Renders the same date card as [NothingWallpaperService] (same position, via
 * [calculateCardRect]) into a static bitmap and sets it as the lock screen wallpaper.
 * The clock and top date row are intentionally omitted since the system lock screen
 * already draws its own clock.
 */
object LockWallpaperGenerator {
    private const val TAG = "LockWallpaperGenerator"

    fun apply(context: Context) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val width = wallpaperManager.desiredMinimumWidth.takeIf { it > 0 } ?: 1080
            val height = wallpaperManager.desiredMinimumHeight.takeIf { it > 0 } ?: 2400
            val bitmap = render(width, height)
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set lock screen wallpaper", e)
        }
    }

    private fun render(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = width / 1080f

        val backgroundPaint = Paint().apply { color = NothingWallpaperColors.BLACK }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Not drawn; only used to reproduce the same card position as the home screen
        // live wallpaper, which anchors the card below a same-size (undrawn) time row.
        val timeMetrics = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            textSize = 150f * scale
        }.fontMetrics

        val geometry = calculateCardRect(width.toFloat(), height.toFloat(), timeMetrics.ascent, timeMetrics.descent)
        val cardRect = RectF(geometry.left, geometry.top, geometry.right, geometry.bottom)
        val radius = 28f * scale

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.BORDER
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * scale
        }
        canvas.drawRoundRect(cardRect, radius, radius, backgroundPaint)
        canvas.drawRoundRect(cardRect, radius, radius, cardBorderPaint)

        val cardLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.GRAY
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            textSize = 24f * scale
        }
        val cardWeekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.ACCENT
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textSize = 34f * scale
        }
        val cardDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.WHITE
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            textSize = 180f * scale
        }

        val pad = 36f * scale
        val innerX = geometry.left + pad
        var innerY = geometry.top + pad

        val now = Date()
        innerY -= cardLabelPaint.fontMetrics.ascent
        canvas.drawText(monthYearText(now), innerX, innerY, cardLabelPaint)

        innerY += 40f * scale
        canvas.drawText(weekdayFullText(now), innerX, innerY, cardWeekdayPaint)

        innerY += 50f * scale - cardDayPaint.fontMetrics.ascent
        canvas.drawText(dayOfMonthText(now), innerX, innerY, cardDayPaint)

        return bitmap
    }
}
