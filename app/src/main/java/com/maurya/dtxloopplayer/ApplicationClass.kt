package com.maurya.dtxloopplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class ApplicationClass:Application() {

    companion object{
        const val CHANNEL_ID  = "channel1"
        const val EXIT  = "exit"
        const val PLAY = "play"
        const val NEXT  = "next"
        const val PREVIOUS  = "previous"
    }

    override fun onCreate() {
        super.onCreate()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,"Now Playing Song",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is Important Channel for showing Songs"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        } else {
            // For older versions of Android, start the service normally
            val intent = Intent(this, MusicService::class.java)
            startService(intent)
        }


    }
}