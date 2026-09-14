package com.sekitakumi.nothingfoldlauncher.wallpaper

data class CardGeometry(val left: Float, val top: Float, val right: Float, val bottom: Float)

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

    val cardWidth = width - margin * 2
    val cardHeight = height * 0.30f
    val cardLeft = margin
    var cardTop = dateY + 70f * scale
    if (cardTop + cardHeight > height - 120f * scale) {
        cardTop = height - 120f * scale - cardHeight
    }
    return CardGeometry(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)
}
