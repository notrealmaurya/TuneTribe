package com.maurya.dtxloopplayer

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityMainBinding
import com.maurya.dtxloopplayer.databinding.PlayerControlsPanelBinding
import com.maurya.dtxloopplayer.databinding.PopupVideoSpeedBinding
import com.maurya.dtxloopplayer.fragments.FavouriteFragment
import com.maurya.dtxloopplayer.fragments.FolderFragment
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.fragments.SearchFragment
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.createMediaPlayer
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.setMusicData
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, MediaControlInterface {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var playerControlsPanelBinding: PlayerControlsPanelBinding

    private val themeList = arrayOf("Light Mode", "Night Mode", "Auto (System Defaults)")

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var mainIntent: Intent

    private lateinit var musicAdapter: AdapterMusic

    //timer
    private var isTimerOn: Boolean = false
    private var timerText: String = ""
    private var timer: Timer? = null

    companion object {
        var favouriteMusicList: ArrayList<MusicDataClass> = ArrayList()


        var musicService: MusicService? = null
        lateinit var viewModel: ViewModelObserver


        var musicListPlayerFragment: ArrayList<MusicDataClass> = arrayListOf()
        var musicPosition: Int = -1
        var isPlaying: Boolean = false
        var boundEnabled: Boolean = false
        var nowPlayingId: String = ""


        private var bindingRef: WeakReference<PlayerControlsPanelBinding>? = null
        private var bindingMainRef: WeakReference<ActivityMainBinding>? = null

        fun getBottomPlayerBinding(): PlayerControlsPanelBinding? {
            return bindingRef?.get()
        }

        fun getActivityMainBinding(): ActivityMainBinding? {
            return bindingMainRef?.get()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        playerControlsPanelBinding = PlayerControlsPanelBinding.bind(activityMainBinding.root)

        setContentView(activityMainBinding.root)
        bindingRef = WeakReference(playerControlsPanelBinding)
        bindingMainRef = WeakReference(activityMainBinding)

        viewModel = ViewModelProvider(this)[ViewModelObserver::class.java]
        sharedPreferenceHelper = SharedPreferenceHelper(this)
        musicAdapter = AdapterMusic(this, arrayListOf())

        activityMainBinding.topLayout.visibility = View.VISIBLE
        playerControlsPanelBinding.playerLayoutVisibility.visibility = View.GONE

        val favouriteListPreference =
            sharedPreferenceHelper.getPlayListSong("myFavouriteYouNoty572noty")
        favouriteMusicList.clear()
        favouriteMusicList.addAll(favouriteListPreference)

        viewModel.setFavouriteList(favouriteMusicList)

        viewModel.favouriteList.observe(this) {
            favouriteMusicList.addAll(it)
        }

        permission()
        initViewPager()
        listeners()

        viewModel.songInfo.observe(this) { musicData ->
            playerControlsPanelBinding.songNameMiniPlayer.text = musicData.musicName
            playerControlsPanelBinding.songArtistMiniPlayer.text = musicData.albumArtist
            Glide.with(this)
                .asBitmap()
                .load(musicData.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.icon_music)
                .into(playerControlsPanelBinding.AlbumArtMiniPlayer)
        }

    }

    private fun handleIntent(intent: Intent?) {


    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent)
            if (!isPlaying) pauseMusic(musicService!!)
        }
    }


    private fun listeners() {

        //top toolbar

        activityMainBinding.SearchMusicViewMainActivity.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.containerMainActivity, SearchFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        playerControlsPanelBinding.root.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", musicPosition)
            ContextCompat.startActivity(this, intent, null)
        }

        playerControlsPanelBinding.queueNowPlayingFragment.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
            val bottomSheetView = layoutInflater.inflate(R.layout.popup_dialog_queue, null)
            val recyclerView =
                bottomSheetView.findViewById<RecyclerView>(R.id.recyclerViewQueueActivity)
            val totalSongsTextView =
                bottomSheetView.findViewById<TextView>(R.id.totalSongsQueueActivity)


            CoroutineScope(Dispatchers.Default).launch {
                musicAdapter = AdapterMusic(
                    this@MainActivity,
                    musicListPlayerFragment,
                    this@MainActivity,
                    queueActivity = true
                )
                withContext(Dispatchers.Main) {
                    recyclerView.apply {
                        setHasFixedSize(true)
                        setItemViewCacheSize(13)
                        layoutManager =
                            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
                        adapter = musicAdapter
                    }

                    totalSongsTextView.text =
                        "Track Queue (${updateTextViewWithItemCount(musicListPlayerFragment.size)})"

                    bottomSheetDialog.setContentView(bottomSheetView)
                    bottomSheetDialog.setCanceledOnTouchOutside(true)
                    val layoutParams = bottomSheetView.layoutParams
                    layoutParams.height =
                        resources.getDimensionPixelSize(R.dimen.fixed_bottom_sheet_height)
                    bottomSheetView.layoutParams = layoutParams
                    val behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.isDraggable = false

                    bottomSheetDialog.show()

                    recyclerView.smoothScrollToPosition(musicPosition)
                }
            }
        }


        playerControlsPanelBinding.playPauseMiniPlayer.setOnClickListener {
            if (isPlaying) pauseMusic(musicService!!)
            else playMusic(musicService!!)
        }

        playerControlsPanelBinding.NextMiniPlayer.setOnClickListener {
            prevNextSong(increment = true, musicService!!)
        }

        activityMainBinding.timerMainActivity.setOnClickListener {
            if (!isTimerOn) {
                timerMainDialog()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Reset Time")
                    .setMessage("Do you want to reset time? ${timerText} min")
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


    override fun onStart() {
        super.onStart()
        doBindService()
    }


    private fun doBindService() {
        mainIntent = Intent(this, MusicService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    // Defines callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (musicService == null) {
                boundEnabled = true
                val binder = service as MusicService.MyBinder
                musicService = binder.currentService()
                musicService!!.audioManager =
                    getSystemService(Context.AUDIO_SERVICE) as AudioManager
                musicService!!.audioManager.requestAudioFocus(
                    musicService,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )

            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundEnabled = false
            musicService = null
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        prevNextSong(increment = true, musicService!!)
        musicService!!.mediaPlayer!!.setOnCompletionListener(this)
    }


    private fun initServiceAndPlaylist(
        playlist: ArrayList<MusicDataClass>,
    ) {
        musicListPlayerFragment.clear()
        musicListPlayerFragment.addAll(playlist)
        createMediaPlayer(musicService!!)
        musicService!!.mediaPlayer!!.setOnCompletionListener(this@MainActivity)
        setMusicData(viewModel)
        playerControlsPanelBinding.playerLayoutVisibility.visibility = View.VISIBLE
    }

    override fun onSongSelected(
        musicList: ArrayList<MusicDataClass>,
        position: Int
    ) {
        musicPosition = position
        musicAdapter.updatePlaybackState(musicList[position].id)
        initServiceAndPlaylist(musicList)
    }


    override fun onSongShuffled(musicList: ArrayList<MusicDataClass>, shuffle: Boolean) {
        musicList.shuffle()
        musicPosition = 0
        musicAdapter.updatePlaybackState(musicList[musicPosition].id)
        initServiceAndPlaylist(musicList)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (!isPlaying && musicService != null) {
            musicService!!.stopForeground(true)
            val mediaPlayer = musicService!!.mediaPlayer
            mediaPlayer?.release()
            musicService = null
            stopService(mainIntent)
            if (boundEnabled) unbindService(connection)
            exitProcess(1)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activityMainBinding.topLayout.visibility = View.VISIBLE
        supportFragmentManager.popBackStack()
    }

    //
    //
    //
    //
    //
    private fun timerMainDialog() {

        val presetDurations = arrayOf("15 min", "30 min", "45 min", "1 hr")
        val timeIntervals = intArrayOf(15, 30, 45, 60)

        val customDuration = "Set Custom Time"

        val customView = LayoutInflater.from(this)
            .inflate(R.layout.popup_video_speed, activityMainBinding.root, false)
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
            pauseMusic(musicService!!)
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
                    playMusic(musicService!!)
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }


    private fun initViewPager() {

        val myAdapter = ViewPagerAdapter(this)
        myAdapter.addFragment(SongsFragment(), "Songs")
        myAdapter.addFragment(ListsFragment(), "Lists")
        myAdapter.addFragment(FolderFragment(), "Folders")


        val viewPager = activityMainBinding.viewPAGER
        viewPager.adapter = myAdapter


        val tabLayout = activityMainBinding.tabLayout
        val tabIndicator = ContextCompat.getDrawable(this, R.drawable.custom_tab_indicator)
        tabLayout.setSelectedTabIndicator(tabIndicator)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = myAdapter.getTitle(position)
        }.attach()

    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.FOREGROUND_SERVICE
                    ), 1
                )
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ), 2
                )
            }
        } else {
        }
    }

    /*Handle permission request results*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 || requestCode == 2) {
            var allPermissionsGranted = true
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (!allPermissionsGranted) {
                showPermissionRequiredDialog()
            } else {
                doBindService()
            }
        }
    }

    private fun showPermissionRequiredDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
            .setMessage("This permission is required to access the app.")
            .setPositiveButton(
                "Go to Settings"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }


}

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val titleList: MutableList<String> = ArrayList()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        titleList.add(title)
    }

    fun getTitle(position: Int): String = titleList[position]
}


