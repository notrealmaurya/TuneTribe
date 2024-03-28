package com.maurya.dtxloopplayer.activities

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.databinding.ActivityPlayerBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogPlayeractivityMenuBinding
import com.maurya.dtxloopplayer.databinding.PopupVideoSpeedBinding
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.getMusicArt
import com.maurya.dtxloopplayer.utils.getMusicDetailsPlayerActivity
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.setMusicData
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private var timer: Timer? = null

    private lateinit var binding: ActivityPlayerBinding

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper


    private lateinit var viewModel: ViewModelObserver

    companion object {

        private var bindingRef: WeakReference<ActivityPlayerBinding>? = null

        fun getPlayerActivityBinding(): ActivityPlayerBinding? {
            return bindingRef?.get()
        }

        //timer
        var isTimerOn: Boolean = false
        var timerText: String = ""

        var repeat: Boolean = false

        var isFavourite: Boolean = false
        var favouriteIndex: Int = -1

        lateinit var loudnessEnhancer: LoudnessEnhancer

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindingRef = WeakReference(binding)

        sharedPreferenceHelper = SharedPreferenceHelper(this)
        viewModel = ViewModelProvider(this)[ViewModelObserver::class.java]

        val savedTheme = sharedPreferenceHelper.getPlayerActivityTheme()

        // Set the Lottie animation based on the saved theme
        if (!savedTheme.isNullOrBlank()) {
            binding.lottiePlayerActivity.setAnimation(savedTheme)
            binding.lottiePlayerActivity.playAnimation()
        }


        MainActivity.musicListPlayerFragment.add(getMusicDetailsPlayerActivity(intent.data!!, this))
        Glide.with(this)
            .load(getMusicArt(MainActivity.musicListPlayerFragment[MainActivity.musicPosition].path))
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(binding.songImagePlayerActivity)
        binding.songNAME.text =
            MainActivity.musicListPlayerFragment[MainActivity.musicPosition].musicName
        binding.songARTIST.text =
            MainActivity.musicListPlayerFragment[MainActivity.musicPosition].albumArtist

        favouriteIndex =
            favouriteChecker(MainActivity.musicListPlayerFragment[MainActivity.musicPosition].id)

        binding.songNAME.isSelected = true
        binding.songARTIST.isSelected = true


        viewModel.songInfo.observe(this) { musicData ->
            binding.songNAME.text = musicData.musicName
            binding.songARTIST.text = musicData.albumArtist
            Glide.with(this)
                .asBitmap()
                .load(musicData.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.icon_music)
                .into(binding.songImagePlayerActivity)
        }

        setMusicData(viewModel)


        listeners()

    }


    private fun listeners() {


        binding.PlayerBackBtn.setOnClickListener {
            finish()
        }

        binding.playPausePlayerActivity.setOnClickListener {
            if (MainActivity.isPlaying) pauseMusic(MainActivity.musicService!!)
            else playMusic(MainActivity.musicService!!)
        }

        binding.nextSongPlayerActivity.setOnClickListener {
            prevNextSong(
                increment = true,
                MainActivity.musicService!!
            )
        }

        binding.prevSongPlayerActivity.setOnClickListener {
            prevNextSong(
                increment = false,
                MainActivity.musicService!!
            )
        }

        binding.seekBARPlayerActivity.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    MainActivity.musicService!!.mediaPlayer!!.seekTo(progress)
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
                Uri.parse(MainActivity.musicListPlayerFragment[MainActivity.musicPosition].path)
            )
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File"))
        }


        //Player Theme
        binding.playerthemePlayerActivity.setOnClickListener {
            val lottieView = binding.lottiePlayerActivity
            val popupMenu =
                PopupMenu(this, binding.playerthemePlayerActivity, R.style.PopUpWindowStyle)
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

                    R.id.playerTheme6 -> {
                        binding.lottiePlayerActivity.setAnimation("wave6.json")
                        binding.lottiePlayerActivity.playAnimation()
                        saveLottieAnimationTheme("wave6.json")
                        true
                    }

                    R.id.playerTheme7 -> {
                        binding.lottiePlayerActivity.setAnimation("wave7.json")
                        binding.lottiePlayerActivity.playAnimation()
                        saveLottieAnimationTheme("wave7.json")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }


        //add favourite
        binding.addFavouritePlayerActivity.setOnClickListener {
            favouriteIndex =
                favouriteChecker(MainActivity.musicListPlayerFragment[MainActivity.musicPosition].id)

            if (isFavourite) {
                isFavourite = false
                binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)
                MainActivity.favouriteMusicList.removeAt(favouriteIndex)
                showToast(this, "Removed from Favourite")
            } else {
                isFavourite = true
                binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
                MainActivity.favouriteMusicList.add(MainActivity.musicListPlayerFragment[MainActivity.musicPosition])
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
        val bottomSheetDialog = BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
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
                MainActivity.musicService!!.mediaPlayer!!.audioSessionId
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


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || requestCode == RESULT_OK) {
            return
        }
    }

    private fun saveLottieAnimationTheme(theme: String) {
        sharedPreferenceHelper.savePlayerActivityTheme(theme)
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
            pauseMusic(MainActivity.musicService!!)
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

}