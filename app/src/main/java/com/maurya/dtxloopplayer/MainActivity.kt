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
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.maurya.dtxloopplayer.activities.FolderTracksActivity
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.databinding.ActivityMainBinding
import com.maurya.dtxloopplayer.databinding.PlayerControlsPanelBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.createMediaPlayer
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.setLayout
import com.maurya.dtxloopplayer.utils.setMusicData
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var playerControlsPanelBinding: PlayerControlsPanelBinding

    private val themeList = arrayOf("Light Mode", "Night Mode", "Auto (System Defaults)")

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var mainIntent: Intent

    private lateinit var musicAdapter: AdapterMusic

    companion object {
        var favouriteMusicList: ArrayList<MusicDataClass> = ArrayList()


        var musicService: MusicService? = null
        lateinit var viewModel: ViewModelObserver


        lateinit var musicListPlayerFragment: ArrayList<MusicDataClass>
        var musicPosition: Int = 0
        var isPlaying: Boolean = false
        var boundEnabled: Boolean = false
        var nowPlayingId: String = ""

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        playerControlsPanelBinding = PlayerControlsPanelBinding.bind(activityMainBinding.root)

        setContentView(activityMainBinding.root)


        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val favouriteListPreference =
            sharedPreferenceHelper.getPlayListSong("myFavouriteYouNoty572noty")
        favouriteMusicList.clear()
        favouriteMusicList.addAll(favouriteListPreference)


        permission()
        initViewPager()
        listeners()

    }

    private fun initializeLayout() {
        musicPosition = intent.getIntExtra("index", 0)

        when (intent.getStringExtra("class")) {

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
                favouriteMusicList,
                shuffle = false
            )

            "FavouriteActivityShuffle" -> initServiceAndPlaylist(
                favouriteMusicList,
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
                    musicListPlayerFragment, shuffle = false
                )
            }

        }

    }

    private fun initServiceAndPlaylist(
        playlist: ArrayList<MusicDataClass>,
        shuffle: Boolean
    ) {
        playerControlsPanelBinding.seekBarMiniPlayer.progress =
            musicService!!.mediaPlayer!!.currentPosition
        playerControlsPanelBinding.seekBarMiniPlayer.max = musicService!!.mediaPlayer!!.duration

        if (isPlaying) playerControlsPanelBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_pause)
        else playerControlsPanelBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_play)

        musicListPlayerFragment = ArrayList()
        musicListPlayerFragment.addAll(playlist)
        if (shuffle) musicListPlayerFragment.shuffle()
        setLayout()
        setMusicData(viewModel)
    }


    private fun listeners() {

        //top toolbar

        activityMainBinding.SearchMusicViewMainActivity.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        playerControlsPanelBinding.root.setOnClickListener {
            sendIntent(
                this,
                position = musicPosition,
                reference = "NowPlaying"
            )
        }

        playerControlsPanelBinding.queueNowPlayingFragment.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(this, R.style.ThemeOverlay_App_BottomSheetDialog)
            val bottomSheetView = layoutInflater.inflate(R.layout.popup_dialog_queue, null)
            val recyclerView =
                bottomSheetView.findViewById<RecyclerView>(R.id.recyclerViewQueueActivity)
            val totalSongsTextView =
                bottomSheetView.findViewById<TextView>(R.id.totalSongsQueueActivity)

            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(13)
            recyclerView.layoutManager = LinearLayoutManager(this)

            musicAdapter =
                AdapterMusic(
                    this,
                    musicListPlayerFragment,
                    queueActivity = true
                )
            recyclerView.adapter = musicAdapter

            recyclerView.smoothScrollToPosition(musicPosition)

            val musicListSize = musicListPlayerFragment.size
            val songText = if (musicListSize == 1) "song" else "songs"
            totalSongsTextView.text = "Track Queue ($musicListSize $songText)"

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.setCanceledOnTouchOutside(true)

            // Set a fixed height for the Bottom Sheet Dialog (e.g., 400dp)
            val layoutParams = bottomSheetView.layoutParams
            layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.fixed_bottom_sheet_height) // Create a dimension resource for the fixed height
            bottomSheetView.layoutParams = layoutParams

            // Set BottomSheetBehavior to fixed height and disable dragging to expand
            val behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false

            bottomSheetDialog.show()

            musicAdapter.notifyDataSetChanged()
        }

        playerControlsPanelBinding.playPauseMiniPlayer.setOnClickListener {
            if (isPlaying) pauseMusic(musicService!!)
            else playMusic(musicService!!)
        }

        playerControlsPanelBinding.NextMiniPlayer.setOnClickListener {
            prevNextSong(increment = true, musicService!!)
        }

    }


    private fun initViewPager() {

        val myAdapter = ViewPagerAdapter(this)
        myAdapter.addFragment(SongsFragment(), "Songs")
        myAdapter.addFragment(ListsFragment(), "Lists")


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
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, which -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!isPlaying && musicService != null) {
            musicService!!.stopForeground(true)
            musicService!!.mediaPlayer!!.release()
            musicService = null
            stopService(mainIntent)
            if (boundEnabled) unbindService(connection)
            exitProcess(1)
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
                musicService!!.seekBarSetup()
                musicService!!.audioManager =
                    getSystemService(Context.AUDIO_SERVICE) as AudioManager
                musicService!!.audioManager.requestAudioFocus(
                    musicService,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )

            }
            createMediaPlayer(musicService!!)
            musicService!!.mediaPlayer!!.setOnCompletionListener(this@MainActivity)
            musicService!!.seekBarSetup()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundEnabled = false
            musicService = null
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        prevNextSong(increment = true, musicService!!)
        musicService!!.mediaPlayer!!.setOnCompletionListener(this)
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


