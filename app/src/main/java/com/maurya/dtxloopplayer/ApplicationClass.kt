package com.maurya.dtxloopplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class ApplicationClass : Application() {

    companion object {
        const val CHANNEL_ID = "channel1"
        const val ACTION_PLAY = "play"
        const val ACTION_NEXT = "next"
        const val ACTION_PREVIOUS = "previous"
    }

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(sharedPreferencesHelper.themeFlag[sharedPreferencesHelper.theme])

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Now Playing Song",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "This is Important Channel for showing Songs"
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        } else {
            // For older versions of Android, start the service normally
            val intent = Intent(this, MusicService::class.java)
            startService(intent)
        }


    }
}