package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Intent

private const val SWIPE_THRESHOLD_PX = 120f

enum class HomeRoute { HOME, DRAWER }

fun shouldCloseDrawerOnNewIntent(intentAction: String?, isDrawerOpen: Boolean): Boolean {
    return isDrawerOpen && intentAction == Intent.ACTION_MAIN
}

fun nextHomeRoute(currentRoute: HomeRoute, dragAccumX: Float, dragAccumY: Float): HomeRoute {
    return when (currentRoute) {
        HomeRoute.HOME -> when {
            dragAccumY < -SWIPE_THRESHOLD_PX -> HomeRoute.DRAWER
            else -> HomeRoute.HOME
        }
        // The drawer is left only via the list's pull-down gesture (DrawerCloseGesture),
        // the home intent, or finishing an edit - never by a bare swipe.
        HomeRoute.DRAWER -> HomeRoute.DRAWER
    }
}
