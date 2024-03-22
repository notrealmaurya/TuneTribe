package com.maurya.dtxloopplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.MusicService
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.databinding.ActivityPlayerBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogPlayeractivityMenuBinding
import com.maurya.dtxloopplayer.databinding.PopupVideoSpeedBinding
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.utils.exitApplication
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.formatDuration
import com.maurya.dtxloopplayer.utils.getMusicArt
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.setSongPosition
import com.maurya.dtxloopplayer.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private lateinit var musicAdapter: AdapterMusic
    private var shuffle: Boolean = false

    private var timer: Timer? = null


    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    companion object {
        lateinit var musicListPlayerActivity: ArrayList<MusicDataClass>
        var musicPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var isInitialized = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding


        //timer
        var isTimerOn: Boolean = false
        var timerText: String = ""

        var repeat: Boolean = false

        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var favouriteIndex: Int = -1

        lateinit var loudnessEnhancer: LoudnessEnhancer

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.songNAME.isSelected = true
        binding.songARTIST.isSelected = true

        // Retrieve the saved Lottie animation theme from SharedPreferences
        sharedPreferenceHelper = SharedPreferenceHelper(this)
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
            binding.songNAME.text = musicListPlayerActivity[musicPosition].musicName
            binding.songARTIST.text = musicListPlayerActivity[musicPosition].albumArtist
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
            loudnessEnhancer = LoudnessEnhancer(musicService!!.mediaPlayer!!.audioSessionId)
            loudnessEnhancer.enabled = true

        } catch (e: Exception) {
            Log.e("MusicService", "Error in CreateMediaPlayerPlayerActivity", e)
        }
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
                SongsFragment.musicList,
                shuffle = false
            )

            "SongsFragmentShuffle" -> initServiceAndPlaylist(
                SongsFragment.musicList,
                shuffle = true
            )

            "FavouriteAdapter" -> initServiceAndPlaylist(
                MainActivity.favouriteMusicList,
                shuffle = false
            )

            "FavouriteActivityShuffle" -> initServiceAndPlaylist(
                MainActivity.favouriteMusicList,
                shuffle = true
            )

            "PlayListActivity" -> initServiceAndPlaylist(
                PlayListActivity.currentPlayListMusicList,
                shuffle = false
            )

            "PlayListActivityShuffle" -> initServiceAndPlaylist(
                PlayListActivity.currentPlayListMusicList,
                shuffle = true
            )

            "folderSongsActivity" -> initServiceAndPlaylist(
                FolderTracksActivity.folderMusicList, shuffle = false

            )

            "folderSongsActivityShuffle" -> initServiceAndPlaylist(
                FolderTracksActivity.folderMusicList, shuffle = true
            )

            "queueActivity" -> {
                initServiceAndPlaylist(
                    musicListPlayerActivity, shuffle = false
                )
            }

        }
        if (musicService != null && !isPlaying) playMusic()
    }

    private fun setLayout() {
        favouriteIndex = favouriteChecker(musicListPlayerActivity[musicPosition].id)

        if (!isFinishing && !isDestroyed) {
            Glide.with(this)
                .load(musicListPlayerActivity[musicPosition].image)
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(binding.songImagePlayerActivity)
        }

        binding.songNAME.text = musicListPlayerActivity[musicPosition].musicName
        binding.songARTIST.text = musicListPlayerActivity[musicPosition].albumArtist
        if (repeat) binding.repeatBtnPlayerActivity.setImageResource(R.drawable.icon_repeat_one)
        if (isFavourite) binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
        else binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)
    }

    private fun initServiceAndPlaylist(
        playlist: ArrayList<MusicDataClass>,
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

    private fun getMusicDetails(contentUri: Uri): MusicDataClass {

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

            return MusicDataClass(
                id = "Unknown",
                path.toString(),
                "Unknown",
                duration,
                "Unknown",
                "Unknown",
                path.toString(),
                "Unknown",
                dateModified
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

        //repeat
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

        //queue
        binding.queuePlayerActivity.setOnClickListener {
            Toast.makeText(this, "Feature Coming Soon", Toast.LENGTH_SHORT).show()
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
                MainActivity.favouriteMusicList.removeAt(favouriteIndex)
                showToast(this, "Removed from Favourite")
            } else {
                isFavourite = true
                binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
                MainActivity.favouriteMusicList.add(musicListPlayerActivity[musicPosition])
                showToast(this, "Added in Favourite")
            }
            sharedPreferenceHelper.savePlayListSong(
                MainActivity.favouriteMusicList, "myFavouriteYouNoty572noty"
            )
            sharedPreferenceHelper.savePlayListSongCount(
                MainActivity.favouriteMusicList.size,
                "myFavouriteYouNoty572notyCount"
            )

        }

        //Player activity menu
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView =
            LayoutInflater.from(this)
                .inflate(R.layout.popup_dialog_playeractivity_menu, binding.root, false)
        val bindingPopUp = PopupDialogPlayeractivityMenuBinding.bind(bottomSheetView)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        binding.menuPlayerActivity.setOnClickListener {
            bottomSheetDialog.show()
        }

        //for equalizer
        bindingPopUp.bottomMenuEqualizerOption.setOnClickListener {
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

        //for audio boost
        bindingPopUp.bottomMenuAudioBoostOption.setOnClickListener {
            bottomSheetDialog.dismiss()

            val popUpDialogBooster = LayoutInflater.from(this)
                .inflate(R.layout.popup_video_speed, binding.root, false)
            val bindingPopUpBooster = PopupVideoSpeedBinding.bind(popUpDialogBooster)

            bindingPopUpBooster.speedSlider.valueFrom = 0f
            bindingPopUpBooster.speedSlider.valueTo = 100f


            MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle)
                .setView(popUpDialogBooster)
                .create()
                .show()

            bindingPopUpBooster.speedSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    loudnessEnhancer.setTargetGain((value * 100).toInt())
                    showToast(this, "Audio boosted to ${value.toInt()}%")
                }
            }

        }

        //for sleep using same slider as in speed
        bindingPopUp.bottomMenuSleepModeOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (!isTimerOn) {
                timerMainDialog()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Reset Time")
                    .setMessage("Do you want to reset time? $timerText min")
                    .setPositiveButton("Yes") { _, _ ->
                        timer?.cancel()
                        isTimerOn = false
                    }
                    .setNeutralButton("Modify Timer") { _, _ ->
                        timerMainDialog()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                        showToast(this, "Sleep timer is reset.")
                        isTimerOn = false
                        timer?.cancel()
                    }
                    .setCancelable(false)
                    .create()
                    .show()

            }

        }

    }

    private fun timerMainDialog() {

        val presetDurations = arrayOf("15 min", "30 min", "45 min", "1 hr")
        val timeIntervals = intArrayOf(15, 30, 45, 60)

        val customDuration = "Set Custom Time"

        val customView = LayoutInflater.from(this)
            .inflate(R.layout.popup_video_speed, binding.root, false)
        val bindingSlider = PopupVideoSpeedBinding.bind(customView)

        bindingSlider.speedSlider.valueFrom = 0f
        bindingSlider.speedSlider.valueTo = presetDurations.size - 1.toFloat()
        bindingSlider.speedSlider.setLabelFormatter { value ->
            presetDurations[value.toInt()]
        }


        MaterialAlertDialogBuilder(this)
            .setTitle("Select Sleep Timer")
            .setView(customView)
            .setNeutralButton(customDuration) { _, _ ->
                timer?.cancel()
                showCustomTimePicker()
            }
            .setPositiveButton("Set") { _, _ ->
                timer?.cancel()
                val selectedDurationIndex = bindingSlider.speedSlider.value.toInt()
                startCountdownTimer(timeIntervals[selectedDurationIndex] * 60 * 1000L)
            }
            .setNegativeButton("Cancel") { self, _ ->
                self.dismiss()
            }
            .create()
            .show()
    }

    private fun showCustomTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Set Custom Time")
            .build()

        timePicker.addOnPositiveButtonClickListener { _ ->
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTimeMillis = (selectedHour * 60 + selectedMinute) * 60 * 1000L

            startCountdownTimer(selectedTimeMillis)
        }

        timePicker.show(supportFragmentManager, "CustomTimePicker")
    }

    private fun startCountdownTimer(durationMillis: Long) {
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                timerExpired()
            }
        }
        timer!!.schedule(task, durationMillis)

        val durationMinutes =
            TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        showToast(this, "Sleep timer set for $durationMinutes min")
        timerText = durationMinutes.toString()

        isTimerOn = true
    }

    private fun timerExpired() {
        runOnUiThread {
            pauseMusic()
            MaterialAlertDialogBuilder(this)
                .setTitle("Timer Expired")
                .setMessage("Do you want to exit the application?")
                .setPositiveButton("Yes") { _, _ ->
                    moveTaskToBack(true)
                    exitProcess(1)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    showToast(this, "Sleep timer is reset.")
                    isTimerOn = false
                    timer?.cancel()
                }
                .setCancelable(false)
                .create()
                .show()
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
            .load(musicListPlayerActivity[musicPosition].image)
            .apply(
                RequestOptions().placeholder(R.drawable.icon_music).centerCrop()
            )
            .into(NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
            musicListPlayerActivity[musicPosition].musicName
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
            musicListPlayerActivity[musicPosition].albumArtist

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