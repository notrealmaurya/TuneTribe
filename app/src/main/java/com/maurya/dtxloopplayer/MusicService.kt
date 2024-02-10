package com.maurya.dtxloopplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.dataEntity.formatDuration
import com.maurya.dtxloopplayer.dataEntity.getMusicArt

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {


    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }


    fun showNotification(playPauseButton: Int, title: String) {
        try {
            val intent = Intent(baseContext, MainActivity::class.java)
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
//            val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)
            val contentIntent = TaskStackBuilder.create(baseContext).run {
                addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, flag)
            }


            val prevIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.PREVIOUS)
            val prevPendingIntent =
                PendingIntent.getBroadcast(
                    baseContext,
                    0,
                    prevIntent,
                    flag
                )

            val playIntent =
                Intent(
                    baseContext,
                    NotificationReceiver::class.java
                ).setAction(ApplicationClass.PLAY)
            val playPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                playIntent,
                flag
            )

            val nextIntent =
                Intent(
                    baseContext,
                    NotificationReceiver::class.java
                ).setAction(ApplicationClass.NEXT)
            val nextPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                nextIntent,
                flag
            )

            val exitIntent =
                Intent(
                    baseContext,
                    NotificationReceiver::class.java
                ).setAction(ApplicationClass.EXIT)
            val exitPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                exitIntent,
                flag
            )

            val musicArt =
                getMusicArt(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].path)
            val image = if (musicArt != null) {
                BitmapFactory.decodeByteArray(musicArt, 0, musicArt.size)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.music)
            }


            val notification =
                NotificationCompat.Builder(
                    this,
                    ApplicationClass.CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.icon_notification_music)
                    .setContentTitle(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].title)
                    .setContentText(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artist)
                    .setLargeIcon(image)
                    .setChannelId(ApplicationClass.CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .addAction(R.drawable.icon_notification_prev, "Previous", prevPendingIntent)
                    .addAction(playPauseButton, title, playPendingIntent)
                    .addAction(R.drawable.icon_notification_next, "Next", nextPendingIntent)
                    .addAction(R.drawable.icon_notification_exit, "Exit", exitPendingIntent)
//                    .setStyle(
//                        androidx.media.app.NotificationCompat.MediaStyle()
//                            .setMediaSession(mediaSession.sessionToken)
//
//                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(contentIntent)
                    .build()


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
                mediaSession.setPlaybackState(playBackState)

                mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                        if (PlayerActivity.isPlaying) {
                            //pause music
                            PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
                            NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
                                R.drawable.icon_play
                            )
                            PlayerActivity.isPlaying = false
                            mediaPlayer!!.pause()
                            showNotification(R.drawable.icon_play, "Play")
                        } else {
                            //play music
                            PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
                            NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
                                R.drawable.icon_pause
                            )
                            PlayerActivity.isPlaying = true
                            mediaPlayer!!.start()
                            showNotification(R.drawable.icon_pause, "Pause")
                        }
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }


                    override fun onSeekTo(pos: Long) {
                        super.onSeekTo(pos)
                        mediaPlayer!!.seekTo(pos.toInt())
                        val playBackStateNew = PlaybackStateCompat.Builder()
                            .setState(
                                PlaybackStateCompat.STATE_PLAYING,
                                mediaPlayer!!.currentPosition.toLong(),
                                playbackSpeed
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .build()
                        mediaSession.setPlaybackState(playBackStateNew)
                    }
                })
            }
            startForeground(13, notification)
        } catch (e: Exception) {
            // Log the error message
            Log.e("MusicService", "Error in showNotification", e)
        }
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

//            PlayerActivity.nowPlayingId = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id
//            PlayerActivity.loudnessEnhancer = LoudnessEnhancer(mediaPlayer!!.audioSessionId)
//            PlayerActivity.loudnessEnhancer.enabled = true
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
        return START_STICKY
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

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
            R.drawable.icon_pause
        )
        mediaPlayer!!.start()
        showNotification(R.drawable.icon_pause, "Pause")
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
            R.drawable.icon_play
        )
        mediaPlayer!!.pause()
        showNotification(R.drawable.icon_play, "Play")
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseMusic()
        // Release audio focus
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
    }



}



