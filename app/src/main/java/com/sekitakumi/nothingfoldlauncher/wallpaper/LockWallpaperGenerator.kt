package com.sekitakumi.nothingfoldlauncher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import java.util.Date

/**
 * Renders the same dot calendar card as [NothingWallpaperService] (same position, via
 * [calculateCardRect]) into a static bitmap and sets it as the lock screen wallpaper.
 * The clock and top date row are intentionally omitted since the system lock screen
 * already draws its own clock.
 */
object LockWallpaperGenerator {
    private const val TAG = "LockWallpaperGenerator"

    /**
     * [width]/[height] should be the real pixel bounds of the display currently
     * showing the lock screen (e.g. `WindowManager.currentWindowMetrics.bounds`),
     * not [WallpaperManager.getDesiredMinimumWidth]/[WallpaperManager.getDesiredMinimumHeight]
     * — on a foldable those can describe a different display than the one being
     * unlocked, which misplaces the card. An explicit [visibleCropHint] covering the
     * whole bitmap is passed so the system does not re-crop it for that display.
     */
    fun apply(context: Context, width: Int, height: Int) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = render(width, height)
            val cropHint = Rect(0, 0, width, height)
            wallpaperManager.setBitmap(bitmap, cropHint, true, WallpaperManager.FLAG_LOCK)
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
        drawDotCalendarCard(canvas, geometry, scale, Date())

        return bitmap
    }
}
