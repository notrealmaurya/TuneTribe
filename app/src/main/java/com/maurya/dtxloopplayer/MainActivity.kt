package com.maurya.dtxloopplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.GsonBuilder
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityMainBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogAboutBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val themeList = arrayOf("Light Mode", "Night Mode", "Auto (System Defaults)")

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper


    companion object {
        var musicList: ArrayList<MusicDataClass> = ArrayList()
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesHelper = SharedPreferenceHelper(this)

//        ListsFragment.musicPlayList = MusicPlayList()
//        // Retrieve Playlist data using shared preferences
//        val jsonStringPlaylist = sharedPreferencesHelper.getPlaylistData()
//        if (jsonStringPlaylist != null) {
//            val dataPlaylist: MusicPlayList =
//                GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlayList::class.java)
//            ListsFragment.musicPlayList = dataPlaylist
//        }

        //NightMode
        var checkedTheme = sharedPreferencesHelper.theme
//        binding.darkModeText.text = "Theme: ${themeList[sharedPreferencesHelper.theme]}"
//
//        binding.darkModeText.setOnClickListener {
//            val dialog = MaterialAlertDialogBuilder(this)
//                .setTitle("Change theme")
//                .setPositiveButton("Ok") { _, _ ->
//                    sharedPreferencesHelper.theme = checkedTheme
//                    AppCompatDelegate.setDefaultNightMode(sharedPreferencesHelper.themeFlag[checkedTheme])
//                    binding.darkModeText.text = "Theme: ${themeList[sharedPreferencesHelper.theme]}"
//                }
//                .setSingleChoiceItems(themeList, checkedTheme) { _, which ->
//                    checkedTheme = which
//                }
//                .setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setCancelable(false)
//                .show()
//
//            dialog.setOnDismissListener {
//                dialog.dismiss()
//            }
//        }


        permission()
        initViewPager()
        listeners()


//        val tempList = findMusicFiles()
        musicList.addAll(musicList)

    }


    private fun listeners() {

        //top toolbar

        binding.SearchMusicViewMainActivity.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


    }


    private fun initViewPager() {

        val myAdapter = ViewPagerAdapter(this)
        myAdapter.addFragment(SongsFragment(), "Songs")
        myAdapter.addFragment(ListsFragment(), "Lists")


        val viewPager = binding.viewPAGER
        viewPager.adapter = myAdapter


        val tabLayout = binding.tabLayout
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
                        Manifest.permission.POST_NOTIFICATIONS
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
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
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

        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }

        // Save Playlist data using shared preferences using the helper
//        val jsonStringPlaylist = GsonBuilder().create().toJson(ListsFragment.musicPlayList)
//        sharedPreferencesHelper.savePlaylistData(jsonStringPlaylist)

        ListsFragment.playListAdapter.notifyDataSetChanged()
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


