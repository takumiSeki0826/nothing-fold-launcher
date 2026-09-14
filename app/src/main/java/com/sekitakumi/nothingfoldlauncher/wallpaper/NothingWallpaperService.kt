package com.sekitakumi.nothingfoldlauncher.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.Date

class NothingWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = NothingEngine()

    private inner class NothingEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var width = 0
        private var height = 0
        private var visible = false

        private val backgroundPaint = Paint().apply { color = NothingWallpaperColors.BLACK }
        private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.WHITE
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        private val dateSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.GRAY
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        private val accentDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NothingWallpaperColors.ACCENT
        }

        private val drawRunnable = Runnable { drawFrame() }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, newWidth: Int, newHeight: Int) {
            super.onSurfaceChanged(holder, format, newWidth, newHeight)
            width = newWidth
            height = newHeight
            drawFrame()
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (visible) {
                drawFrame()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null && width > 0 && height > 0) {
                    render(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            handler.removeCallbacks(drawRunnable)
            if (visible) {
                handler.postDelayed(drawRunnable, 1_000L)
            }
        }

        private fun render(canvas: Canvas) {
            val scale = width / 1080f
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            val margin = 64f * scale
            timePaint.textSize = 150f * scale
            val now = Date()
            val timeString = timeText(now)

            val timeMetrics = timePaint.fontMetrics
            val timeTop = height * 0.16f
            val timeBaseline = timeTop - timeMetrics.ascent
            canvas.drawText(timeString, margin, timeBaseline, timePaint)

            dateSmallPaint.textSize = 30f * scale
            val dateY = timeBaseline + timeMetrics.descent + 40f * scale - timeMetrics.ascent
            val dateString = dateWithWeekdayText(now)
            canvas.drawText(dateString, margin, dateY, dateSmallPaint)

            val dateWidth = dateSmallPaint.measureText(dateString)
            val dotRadius = 4f * scale
            val dotCx = margin + dateWidth + 14f * scale
            val dotCy = dateY - (dateSmallPaint.fontMetrics.ascent + dateSmallPaint.fontMetrics.descent) / 2f
            canvas.drawCircle(dotCx, dotCy, dotRadius, accentDotPaint)

            val geometry = calculateCardRect(width.toFloat(), height.toFloat(), timeMetrics.ascent, timeMetrics.descent)
            drawDotCalendarCard(canvas, geometry, scale, now)
        }
    }
}
