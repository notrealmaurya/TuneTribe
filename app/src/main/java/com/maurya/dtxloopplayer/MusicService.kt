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
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaStyleNotificationHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_NEXT
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PLAY
import com.maurya.dtxloopplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.utils.Versioning
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.formatDuration
import com.maurya.dtxloopplayer.utils.getMusicArt
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
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
                            .setShowActionsInCompactView(1, 2, 3)
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
                    mediaPlayer!!.seekTo(pos.toInt())
                    super.onSeekTo(pos)
                }


                override fun onPlay() {
                    playMusic()
                    super.onPlay()
                }

                override fun onPause() {
                    pauseMusic()
                    super.onPause()
                }

                override fun onSkipToNext() {
                    prevNextSong(increment = true, this@MusicService)
                    super.onSkipToNext()
                }

                override fun onSkipToPrevious() {
                    prevNextSong(increment = false, this@MusicService)
                    super.onSkipToPrevious()
                }

            })

            startForeground(13, notificationBuilder)

        } catch (e: Exception) {
            Log.e("MusicService", "Error in showNotification", e)
        }
    }

    override fun onCreate() {
        super.onCreate()

    }

    fun createMediaPlayer() {
        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].path)
            mediaPlayer!!.prepare()
            PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
            showNotification(R.drawable.icon_pause, "Pause")
            PlayerActivity.binding.durationPLAYEDPlayerActivity.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.durationTOTALPlayerActivity.text =
                formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBARPlayerActivity.progress = 0
            PlayerActivity.binding.seekBARPlayerActivity.max =
                mediaPlayer!!.duration
            PlayerActivity.nowPlayingId =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id
            PlayerActivity.nowPlayingId =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id
            PlayerActivity.loudnessEnhancer = LoudnessEnhancer(mediaPlayer!!.audioSessionId)
            PlayerActivity.loudnessEnhancer.enabled = true
        } catch (e: Exception) {
            Log.e("MusicService", "Error in seekBarSetup", e)
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.durationPLAYEDPlayerActivity.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBARPlayerActivity.progress = mediaPlayer!!.currentPosition
            NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.seekBarMiniPlayer.progress =
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
                    prevNextSong(increment = true, context = this)
                }

                ACTION_PLAY -> {
                    if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()




        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].image)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(PlayerActivity.binding.songImagePlayerActivity)

        PlayerActivity.binding.songNAME.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
        PlayerActivity.binding.songARTIST.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].image)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)

        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].folderName
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist

        playMusic()
        PlayerActivity.favouriteIndex =
            favouriteChecker(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id)
        if (PlayerActivity.isFavourite) PlayerActivity.binding.addFavouritePlayerActivity.setImageResource(
            R.drawable.icon_favourite_added
        )
        else PlayerActivity.binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)

        notifyAdapterSongTextPosition()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMusic()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseMusic()
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
                    0 // Flags, typically 0
                )
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
//                    playMusic()
            }
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        pauseMusic()
        // Release audio focus
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
    }


}



