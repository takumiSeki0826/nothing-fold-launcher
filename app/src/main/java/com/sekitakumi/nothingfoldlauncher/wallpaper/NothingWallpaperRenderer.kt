package com.sekitakumi.nothingfoldlauncher.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.sekitakumi.nothingfoldlauncher.ui.CALENDAR_GRID_TOP_INSET_RATIO
import com.sekitakumi.nothingfoldlauncher.ui.calendarDotGrid
import java.util.Calendar
import java.util.Date

data class CardGeometry(val left: Float, val top: Float, val right: Float, val bottom: Float)

// Above this width the display is treated as a large unfolded/tablet-style
// screen (e.g. a Fold opened flat) rather than a phone, so the card is capped
// to a third of the width instead of nearly spanning it.
private const val WIDE_DISPLAY_WIDTH_THRESHOLD = 1600f

/**
 * Pure geometry for the date card, shared by the home screen live wallpaper and the
 * lock screen static wallpaper so both place the card at the same relative position.
 */
fun calculateCardRect(
    width: Float,
    height: Float,
    timeAscent: Float,
    timeDescent: Float,
): CardGeometry {
    val scale = width / 1080f
    val margin = 64f * scale
    val timeTop = height * 0.16f
    val timeBaseline = timeTop - timeAscent
    val dateY = timeBaseline + timeDescent + 40f * scale - timeAscent

    val isWideDisplay = width > WIDE_DISPLAY_WIDTH_THRESHOLD
    val cardWidth = if (isWideDisplay) width / 3f else width - margin * 2
    val cardHeight = height * 0.30f
    val cardLeft = if (isWideDisplay) (width - cardWidth) / 2f else margin
    var cardTop = dateY + 40f * scale
    if (cardTop + cardHeight > height - 120f * scale) {
        cardTop = height - 120f * scale - cardHeight
    }
    return CardGeometry(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)
}

/**
 * Draws the same dot-matrix mini calendar card as [com.sekitakumi.nothingfoldlauncher.ui.CalendarWidget]
 * (month/year label + a dot per day, today accented), shared by the home screen live
 * wallpaper and the lock screen static wallpaper.
 */
fun drawDotCalendarCard(canvas: Canvas, geometry: CardGeometry, scale: Float, now: Date) {
    val cardRect = RectF(geometry.left, geometry.top, geometry.right, geometry.bottom)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NothingWallpaperColors.CARD_BACKGROUND }
    val radius = 24f * scale
    canvas.drawRoundRect(cardRect, radius, radius, backgroundPaint)

    val pad = 36f * scale
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = NothingWallpaperColors.WHITE
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textSize = 34f * scale
    }
    val labelX = geometry.left + pad
    val labelBaseline = geometry.top + pad - labelPaint.fontMetrics.ascent
    canvas.drawText(monthYearText(now), labelX, labelBaseline, labelPaint)

    val gridTop = labelBaseline + labelPaint.fontMetrics.descent + 24f * scale
    val gridRect = RectF(geometry.left + pad, gridTop, geometry.right - pad, geometry.bottom - pad)
    drawDotGrid(canvas, gridRect, now)
}

private fun drawDotGrid(canvas: Canvas, rect: RectF, now: Date) {
    val calendar = Calendar.getInstance().apply { time = now }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val grid = calendarDotGrid(year, month, today)

    val cellWidth = rect.width() / grid.columns
    val basePitch = rect.height() / grid.rows
    val dotRadius = minOf(cellWidth, basePitch) * 0.28f

    val topInset = rect.height() * CALENDAR_GRID_TOP_INSET_RATIO
    val pitch = basePitch * (1f - CALENDAR_GRID_TOP_INSET_RATIO)

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    for (dot in grid.dots) {
        dotPaint.color = if (dot.isToday) NothingWallpaperColors.ACCENT else NothingWallpaperColors.DOT_GRAY
        val dotRadiusForCell = if (dot.isToday) dotRadius * 1.4f else dotRadius
        val cx = rect.left + dot.col * cellWidth + cellWidth / 2f
        val cy = rect.top + topInset + dot.row * pitch + pitch / 2f
        canvas.drawCircle(cx, cy, dotRadiusForCell, dotPaint)
    }
}
