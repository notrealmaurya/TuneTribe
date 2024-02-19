package com.maurya.dtxloopplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.MusicService
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.databinding.ActivityPlayerBinding
import com.maurya.dtxloopplayer.utils.exitApplication
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.formatDuration
import com.maurya.dtxloopplayer.utils.getMusicArt
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.setSongPosition
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount


class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private lateinit var musicAdapter: MusicAdapter
    private var shuffle: Boolean = false


    companion object {
        lateinit var musicListPlayerActivity: ArrayList<MusicData>
        var musicPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var isInitialized = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var favouriteIndex: Int = -1

        lateinit var loudnessEnhancer: LoudnessEnhancer

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        isInitialized = true

        musicAdapter =
            MusicAdapter(this, FavouriteActivity.favouriteSongs, favouriteActivity = true)


        binding.songNAME.isSelected = true
        binding.songARTIST.isSelected = true

        // Retrieve the saved Lottie animation theme from SharedPreferences
        val sharedPreferenceHelper = SharedPreferenceHelper(this)
        val savedTheme = sharedPreferenceHelper.getPlayerActivityTheme()

        // Set the Lottie animation based on the saved theme
        if (!savedTheme.isNullOrBlank()) {
            binding.lottiePlayerActivity.setAnimation(savedTheme)
            binding.lottiePlayerActivity.playAnimation()
        }


        if (intent.data?.scheme.contentEquals("content")) {
            musicPosition = 0
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPlayerActivity = ArrayList()
            musicListPlayerActivity.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getMusicArt(musicListPlayerActivity[musicPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(binding.songImagePlayerActivity)
            binding.songNAME.text = musicListPlayerActivity[musicPosition].title
            binding.songARTIST.text = musicListPlayerActivity[musicPosition].artist
        } else {
            initializeLayout()
        }

        listeners()

    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPlayerActivity[musicPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            binding.durationPLAYEDPlayerActivity.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.durationTOTALPlayerActivity.text =
                formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBARPlayerActivity.progress = 0
            binding.seekBARPlayerActivity.max = musicService!!.mediaPlayer!!.duration
            NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.seekBarMiniPlayer.progress =
                0
            NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.seekBarMiniPlayer.max =
                musicService!!.mediaPlayer!!.duration

            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPlayerActivity[musicPosition].id
            playMusic()
//            loudnessEnhancer = LoudnessEnhancer(musicService!!.mediaPlayer!!.audioSessionId)
//            loudnessEnhancer.enabled = true

        } catch (e: Exception) {
            Log.e("MusicService", "Error in CreateMediaPlayerPlayerActivity", e)
        }
    }

    private fun setLayout() {

        favouriteIndex = favouriteChecker(musicListPlayerActivity[musicPosition].id)

        val bottomSheetDialog =
            BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
        val bottomSheetView: View =
            layoutInflater.inflate(R.layout.bottomsheet_playeractivity_menu, null)
        val bottomMenuSleepModeOption =
            bottomSheetView.findViewById<TextView>(R.id.bottomMenuSleepModeOption)

        if (!isFinishing && !isDestroyed) {
            Glide.with(this)
                .load(musicListPlayerActivity[musicPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(binding.songImagePlayerActivity)
        }

        binding.songNAME.text = musicListPlayerActivity[musicPosition].title
        binding.songARTIST.text = musicListPlayerActivity[musicPosition].artist
        if (repeat) binding.repeatBtnPlayerActivity.setImageResource(R.drawable.icon_repeat_one)
        if (min15 || min30 || min60) bottomMenuSleepModeOption.text = "Stop timer"
        if (isFavourite) binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
        else binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)
    }

    private fun initializeLayout() {
        musicPosition = intent.getIntExtra("index", 0)

        when (intent.getStringExtra("class")) {

            "NowPlaying" -> {
                setLayout()
                binding.durationPLAYEDPlayerActivity.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.durationTOTALPlayerActivity.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBARPlayerActivity.progress =
                    musicService!!.mediaPlayer!!.currentPosition
                binding.seekBARPlayerActivity.max = musicService!!.mediaPlayer!!.duration
                NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.seekBarMiniPlayer.progress =
                    musicService!!.mediaPlayer!!.currentPosition
                NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.seekBarMiniPlayer.max =
                    musicService!!.mediaPlayer!!.duration

                if (isPlaying) binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
                else binding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
            }


            "MusicAdapterSearch" -> initServiceAndPlaylist(
                SearchActivity.musicListSearch,
                shuffle = false
            )

            "SongsFragment" -> initServiceAndPlaylist(
                MainActivity.tempList,
                shuffle = false
            )

            "SongsFragmentShuffle" -> initServiceAndPlaylist(
                MainActivity.tempList,
                shuffle = true
            )

            "FavouriteAdapter" -> initServiceAndPlaylist(
                FavouriteActivity.favouriteSongs,
                shuffle = false
            )

            "FavouriteActivityShuffle" -> initServiceAndPlaylist(
                FavouriteActivity.favouriteSongs,
                shuffle = true
            )

            "PlayListActivity" -> initServiceAndPlaylist(
                ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList,
                shuffle = false
            )

            "PlayListActivityShuffle" -> initServiceAndPlaylist(
                ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList,
                shuffle = true
            )

            "folderSongsActivity" -> initServiceAndPlaylist(
                FolderTracksActivity.folderMusicFiles as ArrayList<MusicData>, shuffle = false

            )

            "folderSongsActivityShuffle" -> initServiceAndPlaylist(
                FolderTracksActivity.folderMusicFiles as ArrayList<MusicData>, shuffle = true
            )

            "queueActivity" -> {
                initServiceAndPlaylist(
                    musicListPlayerActivity, shuffle = false
                )
            }

        }
        if (musicService != null && !isPlaying) playMusic()
    }

    private fun initServiceAndPlaylist(
        playlist: ArrayList<MusicData>,
        shuffle: Boolean,
        playNext: Boolean = false
    ) {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPlayerActivity = ArrayList()
        musicListPlayerActivity.addAll(playlist)
        if (shuffle) musicListPlayerActivity.shuffle()
        setLayout()
    }

    private fun getMusicDetails(contentUri: Uri): MusicData {

        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED  // Include the DATE_ADDED column
            )
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedColumn =
                cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED) // Get the index of DATE_ADDED

            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            val dateModified =
                dateAddedColumn?.let { cursor.getLong(it) } ?: 0 // Handle if DATE_ADDED is null

            return MusicData(
                id = "Unknown",
                title = path.toString(),
                album = "Unknown",
                artist = "Unknown",
                duration = duration,
                artUri = "Unknown",
                path = path.toString(),
                dateModified = dateModified
            )
        } finally {
            cursor?.close()
        }
    }

    private fun listeners() {

        binding.PlayerBackBtn.setOnClickListener {
            finish()
        }

        binding.playPausePlayerActivity.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.nextSongPlayerActivity.setOnClickListener { prevNextMusic(increment = true) }

        binding.prevSongPlayerActivity.setOnClickListener { prevNextMusic(increment = false) }

        binding.seekBARPlayerActivity.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    val playPauseIcon = if (isPlaying) {
                        R.drawable.icon_pause
                    } else {
                        R.drawable.icon_play
                    }

                    val playPauseTitle = if (isPlaying) {
                        "Pause"
                    } else {
                        "Play"
                    }

                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(playPauseIcon, playPauseTitle)
                }
            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })


        binding.repeatBtnPlayerActivity.setOnClickListener {
            if (!repeat) {
                repeat = true
                Toast.makeText(this, "Repeat one", Toast.LENGTH_SHORT).show()
                binding.repeatBtnPlayerActivity.setImageResource(R.drawable.icon_repeat_one)
            } else {
                repeat = false
                Toast.makeText(this, "Repeat all", Toast.LENGTH_SHORT).show()
                binding.repeatBtnPlayerActivity.setImageResource(R.drawable.icon_repeat_all)
            }

        }

        binding.queuePlayerActivity.setOnClickListener {
            Toast.makeText(this, "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }


        //BottomSheetDialog
        val bottomSheetDialog =
            BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
        val bottomSheetView: View =
            layoutInflater.inflate(R.layout.bottomsheet_playeractivity_menu, null)
        val bottomMenuAudioBoostOption =
            bottomSheetView.findViewById<TextView>(R.id.bottomMenuAudioBoostOption)
        val bottomMenuSleepModeOption =
            bottomSheetView.findViewById<TextView>(R.id.bottomMenuSleepModeOption)
        val bottomMenuEqualizerOption =
            bottomSheetView.findViewById<TextView>(R.id.bottomMenuEqualizerOption)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        binding.menuPlayerActivity.setOnClickListener {
            bottomSheetDialog.show()
        }

        bottomMenuEqualizerOption.setOnClickListener {
            val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            equalizerIntent.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                musicService!!.mediaPlayer!!.audioSessionId
            )

            equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            equalizerIntent.putExtra(
                AudioEffect.EXTRA_CONTENT_TYPE,
                AudioEffect.CONTENT_TYPE_MUSIC
            )
            try {
                startActivityForResult(equalizerIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer feature not Supported", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        val sleepModeDialog =
            BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
        val sleepModeSheetView: View =
            layoutInflater.inflate(R.layout.bottomsheet_playeractivity_menu_sleepmode, null)
        sleepModeDialog.setContentView(sleepModeSheetView)
        sleepModeDialog.setCanceledOnTouchOutside(true)

        bottomMenuSleepModeOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            val timer = min15 || min30 || min60
            if (!timer) {
                sleepModeDialog.show()
            } else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to Stop Timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        bottomMenuSleepModeOption.text = "Sleep Mode"
                    }
                    .setNegativeButton("No") { sleepModeDialog, _ ->
                        sleepModeDialog.dismiss()
                    }

                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        val sleepMode15Min =
            sleepModeSheetView.findViewById<TextView>(R.id.TimerMenu15min)
        val sleepMode30Min =
            sleepModeSheetView.findViewById<TextView>(R.id.TimerMenu30min)
        val sleepMode60Min =
            sleepModeSheetView.findViewById<TextView>(R.id.TimerMenu60min)

        sleepMode15Min.setOnClickListener {
            bottomMenuSleepModeOption.text = "Stop timer"
            min15 = true
            Thread {
                Thread.sleep((15 * 60000).toLong())
                if (min15) exitApplication()
            }.start()
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            sleepModeDialog.dismiss()
        }

        sleepMode30Min.setOnClickListener {
            bottomMenuSleepModeOption.text = "Stop timer"
            min30 = true
            Thread {
                Thread.sleep((30 * 60000).toLong())
                if (min30) exitApplication()
            }.start()
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            sleepModeDialog.dismiss()
        }

        sleepMode60Min.setOnClickListener {
            bottomMenuSleepModeOption.text = "Stop timer"
            min60 = true
            Thread {
                Thread.sleep((60 * 60000).toLong())
                if (min60) exitApplication()
            }.start()
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            sleepModeDialog.dismiss()
        }

        //share music
        binding.sharePlayerActivity.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse(musicListPlayerActivity[musicPosition].path)
            )
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File"))
        }


        //Player Theme
        binding.playerthemePlayerActivity.setOnClickListener {
            val lottieView = binding.lottiePlayerActivity
            val popupMenu = PopupMenu(this, binding.playerthemePlayerActivity)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.player_theme_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.playerTheme1 -> {
                        lottieView.setAnimation("wave.json")
                        lottieView.playAnimation()
                        saveLottieAnimationTheme("wave.json")
                        true
                    }

                    R.id.playerTheme2 -> {
                        lottieView.setAnimation("wave2.json")
                        lottieView.playAnimation()
                        saveLottieAnimationTheme("wave2.json")
                        true
                    }

                    R.id.playerTheme3 -> {
                        binding.lottiePlayerActivity.setAnimation("wave3.json")
                        binding.lottiePlayerActivity.playAnimation()
                        saveLottieAnimationTheme("wave3.json")
                        true
                    }

                    R.id.playerTheme4 -> {
                        binding.lottiePlayerActivity.setAnimation("wave4.json")
                        binding.lottiePlayerActivity.playAnimation()
                        saveLottieAnimationTheme("wave4.json")
                        true
                    }

                    R.id.playerTheme5 -> {
                        binding.lottiePlayerActivity.setAnimation("wave5.json")
                        binding.lottiePlayerActivity.playAnimation()
                        saveLottieAnimationTheme("wave5.json")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }


        //add favourite
        binding.addFavouritePlayerActivity.setOnClickListener {
            favouriteIndex = favouriteChecker(musicListPlayerActivity[musicPosition].id)
            if (isFavourite) {
                isFavourite = false
                binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)
                FavouriteActivity.favouriteSongs.removeAt(favouriteIndex)
                Toast.makeText(this, "Removed from Favourite", Toast.LENGTH_SHORT).show()
                updateTextViewWithItemCount(
                    musicAdapter,
                    ListsFragment.fragmentListsBinding.ListsMyFavouritesSize
                )
            } else {
                isFavourite = true
                binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
                FavouriteActivity.favouriteSongs.add(musicListPlayerActivity[musicPosition])
                Toast.makeText(this, "Added in Favourite", Toast.LENGTH_SHORT).show()
                updateTextViewWithItemCount(
                    musicAdapter,
                    ListsFragment.fragmentListsBinding.ListsMyFavouritesSize
                )
            }

        }

    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        setLayout()

        //for refreshing now playing image & text on song completion
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songNameMiniPlayer.isSelected =
            true
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songArtistMiniPlayer.isSelected =
            true
        Glide.with(applicationContext)
            .load(musicListPlayerActivity[musicPosition].artUri)
            .apply(
                RequestOptions().placeholder(R.drawable.icon_music).centerCrop()
            )
            .into(NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
            musicListPlayerActivity[musicPosition].title
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
            musicListPlayerActivity[musicPosition].artist

        notifyAdapterSongTextPosition()

    }

    private fun playMusic() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
        musicService!!.showNotification(R.drawable.icon_pause, "Pause")
        notifyAdapterSongTextPosition()
        val lottieView = binding.lottiePlayerActivity
        lottieView.playAnimation()
    }

    private fun pauseMusic() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
        musicService!!.showNotification(R.drawable.icon_play, "Play")
        notifyAdapterSongTextPosition()
        val lottieView = binding.lottiePlayerActivity
        lottieView.pauseAnimation()
    }

    private fun prevNextMusic(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
            notifyAdapterSongTextPosition()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
            notifyAdapterSongTextPosition()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || requestCode == RESULT_OK) {
            return
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.seekBarSetup()
            musicService!!.audioManager =
                getSystemService(Context.AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )

        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    private fun saveLottieAnimationTheme(theme: String) {
        val sharedPreferenceHelper = SharedPreferenceHelper(this)
        sharedPreferenceHelper.setPlayerActivityTheme(theme)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPlayerActivity[musicPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}