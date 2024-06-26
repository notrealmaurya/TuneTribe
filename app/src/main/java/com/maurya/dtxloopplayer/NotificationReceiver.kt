package com.maurya.dtxloopplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_NEXT
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PLAY
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.setSongPosition

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            ACTION_PREVIOUS -> if (MainActivity.musicListPlayerFragment.size > 1) {
                prevNextSong(increment = false, MusicService())
            }

            ACTION_PLAY -> {
                if (MainActivity.isPlaying) pauseMusic(MainActivity.musicService!!) else playMusic(
                    MainActivity.musicService!!
                )
            }

            ACTION_NEXT -> if (MainActivity.musicListPlayerFragment.size > 1) {
                prevNextSong(increment = true, MusicService())
            }

        }
    }


}