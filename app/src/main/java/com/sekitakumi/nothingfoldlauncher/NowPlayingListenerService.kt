package com.sekitakumi.nothingfoldlauncher

import android.service.notification.NotificationListenerService

/**
 * MediaSessionManager.getActiveSessions()を呼ぶために必要な、通知アクセス権限の入り口。
 * 通知そのものは処理しない。
 */
class NowPlayingListenerService : NotificationListenerService()
