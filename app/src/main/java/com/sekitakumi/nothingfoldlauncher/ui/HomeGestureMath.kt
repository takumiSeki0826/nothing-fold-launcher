package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Intent

private const val SWIPE_THRESHOLD_PX = 120f

enum class HomeRoute { HOME, DRAWER, EQ }

fun shouldCloseDrawerOnNewIntent(intentAction: String?, isDrawerOpen: Boolean): Boolean {
    return isDrawerOpen && intentAction == Intent.ACTION_MAIN
}

fun nextHomeRoute(currentRoute: HomeRoute, dragAccumX: Float, dragAccumY: Float): HomeRoute {
    return when (currentRoute) {
        HomeRoute.HOME -> when {
            dragAccumY < -SWIPE_THRESHOLD_PX -> HomeRoute.DRAWER
            dragAccumX < -SWIPE_THRESHOLD_PX -> HomeRoute.EQ
            else -> HomeRoute.HOME
        }
        HomeRoute.DRAWER -> if (dragAccumX < -SWIPE_THRESHOLD_PX || dragAccumY > SWIPE_THRESHOLD_PX) {
            HomeRoute.HOME
        } else {
            HomeRoute.DRAWER
        }
        HomeRoute.EQ -> if (dragAccumX > SWIPE_THRESHOLD_PX) HomeRoute.HOME else HomeRoute.EQ
    }
}
