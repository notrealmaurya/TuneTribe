package com.maurya.dtxloopplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaStyleNotificationHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_NEXT
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PLAY
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.utils.Versioning
import com.maurya.dtxloopplayer.utils.createMediaPlayer
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.formatDuration
import com.maurya.dtxloopplayer.utils.getMusicArt
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.setSongPosition
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {


    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager


    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(this@MusicService, packageName)

        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }


    fun showNotification(playPauseButton: Int, title: String) {
        try {
            val openPlayerIntent = Intent(this, MainActivity::class.java)
            openPlayerIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            var flags = 0
            if (Versioning.isMarshmallow()) flags = PendingIntent.FLAG_IMMUTABLE or 0

            val contentIntent = PendingIntent.getActivity(this, 0, openPlayerIntent, flags)

            val prevIntent = Intent(
                this,
                NotificationReceiver::class.java
            ).setAction(ACTION_PREVIOUS)
            val prevPendingIntent =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    prevIntent,
                    flags
                )

            val playIntent =
                Intent(
                    this,
                    NotificationReceiver::class.java
                ).setAction(ACTION_PLAY)
            val playPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                playIntent,
                flags
            )

            val nextIntent =
                Intent(
                    this,
                    NotificationReceiver::class.java
                ).setAction(ACTION_NEXT)
            val nextPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                nextIntent,
                flags
            )


            val musicArt =
                getMusicArt(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].path)
            val image: Bitmap?
            if (musicArt != null) {
                image = BitmapFactory.decodeByteArray(musicArt, 0, musicArt.size)
            } else {
                val defaultColor = Color.parseColor("#081747")
                val width = 100
                val height = 100
                image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                image.eraseColor(defaultColor)
            }


            val notificationBuilder =
                NotificationCompat.Builder(
                    this,
                    ApplicationClass.CHANNEL_ID
                )
                    .setContentIntent(contentIntent)
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSilent(true)
                    .setShowWhen(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(PlayerActivity.isPlaying)
                    .setSmallIcon(R.drawable.icon_music)
                    .setLargeIcon(image)
                    .setContentTitle(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName)
                    .setContentText(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist)
                    .addAction(R.drawable.icon_notification_prev, "Prev", prevPendingIntent)
                    .addAction(playPauseButton, title, playPendingIntent)
                    .addAction(R.drawable.icon_notification_next, "Next", nextPendingIntent)
                    .setChannelId(ApplicationClass.CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setStyle(
                        MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2)
                    )
                    .setOnlyAlertOnce(true)
                    .build()


            val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F

            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )

            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()

            mediaSession.setPlaybackState(playBackState)

            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                }

                override fun onPlay() {
                    super.onPlay()
                    playMusic(this@MusicService)
                }

                override fun onPause() {
                    super.onPause()
                    pauseMusic(this@MusicService)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    prevNextSong(increment = true, this@MusicService)
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    prevNextSong(increment = false, this@MusicService)
                }

            })

            startForeground(13, notificationBuilder)

        } catch (e: Exception) {
            Log.e("MusicService", "Error in showNotification", e)
        }
    }


    fun seekBarSetup() {
        val playerBinding = PlayerActivity.getPlayerActivityBinding()
        val nowPlayingBottomBinding = NowPlayingBottomFragment.getNowPlayingFragmentBinding()

        runnable = Runnable {
            playerBinding?.durationPLAYEDPlayerActivity?.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            playerBinding?.seekBARPlayerActivity?.progress = mediaPlayer!!.currentPosition
            nowPlayingBottomBinding?.seekBarMiniPlayer?.progress =
                mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }


    //for making persistent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PREVIOUS -> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                    prevNextSong(false, this)
                }

                ACTION_NEXT -> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                    prevNextSong(increment = true, this)
                }

                ACTION_PLAY -> {
                    if (PlayerActivity.isPlaying) pauseMusic(this@MusicService) else playMusic(this@MusicService)
                }
            }
        }
        return START_NOT_STICKY
    }


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMusic(this@MusicService)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseMusic(this@MusicService)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                val currentVolume =
                    PlayerActivity.musicService!!.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume =
                    PlayerActivity.musicService!!.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                val duckedVolume = maxVolume / 2

                PlayerActivity.musicService!!.audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    duckedVolume,
                    0
                )
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
//                    playMusic()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        pauseMusic(this@MusicService)
        // Release audio focus
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
    }


}



