package com.sekitakumi.nothingfoldlauncher

import android.service.notification.NotificationListenerService

/**
 * Entry point for the notification access permission required to call
 * MediaSessionManager.getActiveSessions(). Does not process notifications itself.
 */
class NowPlayingListenerService : NotificationListenerService()
