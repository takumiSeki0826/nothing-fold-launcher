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

    // Nudges the card down slightly on the lock screen only, relative to the shared
    // position used by the home screen live wallpaper.
    private const val CARD_DOWN_SHIFT = 90f

    // On the unfolded (wide) display only, cancels part of the lock screen's own
    // down-shift so the card sits a bit higher there. The folded cover display is
    // unaffected since it never counts as wide.
    private const val WIDE_DISPLAY_UP_SHIFT = 120f

    // On the folded cover display's lock screen only, the shared geometry's
    // near-full-width "banner" card formula (see the non-wide branch of
    // [calculateCardRect]) reads as a flatter, much less rounded shape than the
    // unfolded display's card, since [drawDotCalendarCard]'s radius/padding are
    // driven by `scale` rather than by the card's own width: the unfolded card's
    // width/3 formula happens to make radius = cardWidth/15 and pad = cardWidth/10,
    // while the cover display's near-full-width formula makes both far smaller
    // relative to the (much wider) card. This fraction picks a narrower cover-display
    // card width instead, chosen so its aspect ratio matches the unfolded card's
    // (both ultimately width/height-ratio-driven, tuned for this device's two real
    // displays), and `cardScale` below reproduces the same cardWidth/15 and
    // cardWidth/10 radius/pad ratios so the two displays' cards read as the same
    // design at different sizes.
    private const val FOLDED_DISPLAY_CARD_WIDTH_FRACTION = 0.7f

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

        val baseGeometry = calculateCardRect(width.toFloat(), height.toFloat(), timeMetrics.ascent, timeMetrics.descent)
        val downShift = (CARD_DOWN_SHIFT * scale).coerceAtMost(
            height - 120f * scale - baseGeometry.bottom
        ).coerceAtLeast(0f)
        val isWideDisplay = width > WIDE_DISPLAY_WIDTH_THRESHOLD
        val shift = if (isWideDisplay) downShift - WIDE_DISPLAY_UP_SHIFT * scale else downShift
        var geometry = CardGeometry(
            baseGeometry.left,
            baseGeometry.top + shift,
            baseGeometry.right,
            baseGeometry.bottom + shift,
        )
        var cardScale = scale
        if (!isWideDisplay) {
            val cardWidth = width * FOLDED_DISPLAY_CARD_WIDTH_FRACTION
            val cardLeft = (width - cardWidth) / 2f
            geometry = CardGeometry(cardLeft, geometry.top, cardLeft + cardWidth, geometry.bottom)
            cardScale = cardWidth / 360f
        }
        drawDotCalendarCard(canvas, geometry, cardScale, Date())

        return bitmap
    }
}
