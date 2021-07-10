package com.example.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass: Application() {
    companion object{
        const val CHANNEL_ID_1 = "channel1"
        const val CHANNEL_ID_2 = "channel2"
        const val ACTION_PREVIOUS = "actionprevious"
        const val ACTION_NEXT = "actionnext"
        const val ACTION_PLAY = "actionplay"
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            var channel1 = NotificationChannel(CHANNEL_ID_1, "channel(1)", NotificationManager.IMPORTANCE_LOW)
            channel1.description = "Channel 1 Desc.."
            var channel2 = NotificationChannel(CHANNEL_ID_2, "channel(2)", NotificationManager.IMPORTANCE_LOW)
            channel2.description = "Channel 2 Desc.."
            var notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
        }
    }
}