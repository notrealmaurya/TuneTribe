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
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.utils.exitApplication
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.setSongPosition

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            ACTION_PREVIOUS -> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                prevNextSong(increment = false, MusicService())
            }

            ACTION_PLAY -> {
                if (PlayerActivity.isPlaying) pauseMusic(PlayerActivity.musicService!!) else playMusic(PlayerActivity.musicService!!)
            }

            ACTION_NEXT-> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                prevNextSong(increment = true, MusicService())
            }

        }
    }






}