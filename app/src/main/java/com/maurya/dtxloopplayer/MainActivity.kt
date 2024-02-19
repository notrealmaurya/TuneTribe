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
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.database.MusicPlayList
import com.maurya.dtxloopplayer.databinding.ActivityMainBinding
import com.maurya.dtxloopplayer.databinding.PopupAboutDialogBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import java.io.File
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val themeList = arrayOf("Light Mode", "Night Mode", "Auto (System Defaults)")
    private lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    companion object {
        val tempList = ArrayList<MusicData>()
        var musicList: ArrayList<MusicData> = ArrayList()
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

        ListsFragment.musicPlayList = MusicPlayList()
        // Retrieve Playlist data using shared preferences
        val jsonStringPlaylist = sharedPreferencesHelper.getPlaylistData()
        if (jsonStringPlaylist != null) {
            val dataPlaylist: MusicPlayList =
                GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlayList::class.java)
            ListsFragment.musicPlayList = dataPlaylist
        }

        binding.navView.visibility = View.VISIBLE

        //NightMode
        var checkedTheme = sharedPreferencesHelper.theme
        binding.darkModeText.text = "Theme: ${themeList[sharedPreferencesHelper.theme]}"

        binding.darkModeText.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("Change theme")
                .setPositiveButton("Ok") { _, _ ->
                    sharedPreferencesHelper.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferencesHelper.themeFlag[checkedTheme])
                    binding.darkModeText.text = "Theme: ${themeList[sharedPreferencesHelper.theme]}"
                }
                .setSingleChoiceItems(themeList, checkedTheme) { _, which ->
                    checkedTheme = which
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()

            dialog.setOnDismissListener {
                dialog.dismiss()
            }
        }


        permission()
        initViewPager()
        listeners()


        val tempList = findMusicFiles()
        musicList.addAll(tempList)

    }

    @SuppressLint("Range")
    private fun findMusicFiles(): ArrayList<MusicData> {
        tempList.clear()
        val uniquePaths = HashSet<String>()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortingList[sortOrder],
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val idSF = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val titleSF =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val albumSF =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistSF =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationSF =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val pathSF =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val albumIDSF =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()

                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriSF = Uri.withAppendedPath(uri, albumIDSF).toString()

                    val dateSF =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED))

                    if (durationSF >= 20000) { // 20 seconds in milliseconds
                        val music = MusicData(
                            id = idSF,
                            title = titleSF,
                            album = albumSF,
                            artist = artistSF,
                            duration = durationSF,
                            path = pathSF,
                            artUri = artUriSF,
                            dateModified = dateSF
                        )

                        val file = File(music.path)
                        if (file.exists() && uniquePaths.add(music.path)) {
                            tempList.add(music)
                        }
                    }
                } while (cursor.moveToNext())
            }
        }

        return tempList
    }


    private fun listeners() {

        //top toolbar
        binding.menuImageView.setOnClickListener(View.OnClickListener {
            binding.MainDrawerLayout.openDrawer(GravityCompat.START)

        })

        binding.SearchMusicViewMainActivity.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_feedback -> {
                    val websiteUrl =
                        "https://docs.google.com/forms/d/e/1FAIpQLSfRsCpO9jc0t61V6E5IkjH6L0HSoWmk2LQdy0EPJ1SmBL7_hQ/viewform"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                    startActivity(intent)
                }

                R.id.nav_Settings -> {

                }

                R.id.nav_about -> {
                    val popUpDialog = LayoutInflater.from(this)
                        .inflate(R.layout.popup_about_dialog, binding.root, false)
                    val bindingPopUp = PopupAboutDialogBinding.bind(popUpDialog)
                    val dialog =
                        MaterialAlertDialogBuilder(this).setView(popUpDialog)
                            .setOnCancelListener {

                            }
                            .create()

                    bindingPopUp.aboutDialogThankyouButton.setOnClickListener {
                        dialog.dismiss()
                    }

                    val textView = bindingPopUp.spannableTextViewDialog
                    val spannableString = SpannableString("If you'd like to share your thoughts or provide Feedback , please feel free to do so. Your input is valuable, and I'd appreciate hearing from you.❤\uFE0F\"\n ")

                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val websiteUrl =
                                "https://forms.gle/4gC2XzHDCaio7hUh8"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                            startActivity(intent)
                        }
                    }

                    spannableString.setSpan(
                        clickableSpan,
                        48, 56,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val blueColor = Color.BLUE
                    spannableString.setSpan(
                        ForegroundColorSpan(blueColor),
                        48, 56,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textView.text = spannableString
                    textView.movementMethod = LinkMovementMethod.getInstance()

                    dialog.show()
                }

            }
            menuItem.isChecked = false
            binding.MainDrawerLayout.closeDrawer(GravityCompat.START)
            true // Return true to indicate that the click was handled
        }


    }


    private fun initViewPager() {

        val myAdapter = ViewPagerAdapter(this)
        myAdapter.addFragment(ListsFragment(), "Lists")
        myAdapter.addFragment(SongsFragment(), "Songs")


        val viewPager = binding.viewPAGER
        viewPager.adapter = myAdapter


        val tabLayout = binding.tabLayout
        val tabIndicator = ContextCompat.getDrawable(this, R.drawable.custom_tab_indicator)
        tabLayout.setSelectedTabIndicator(tabIndicator)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = myAdapter.getTitle(position)
        }.attach()


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                // Depending on the tab position, enable or disable scrolling
                when (position) {
                    0 -> disableCollapsingToolbarScrolling()
                    1 -> enableCollapsingToolbarScrolling()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


    }

    private fun disableCollapsingToolbarScrolling() {
        val toolbarLayoutParams =
            binding.collapsingtoolbarlayout.layoutParams as AppBarLayout.LayoutParams
        toolbarLayoutParams.scrollFlags = 0
        binding.collapsingtoolbarlayout.layoutParams = toolbarLayoutParams
        val appBarLayoutParams = binding.appbar.layoutParams as CoordinatorLayout.LayoutParams
        appBarLayoutParams.behavior = null
        binding.appbar.layoutParams = appBarLayoutParams
    }

    private fun enableCollapsingToolbarScrolling() {
        val toolbarLayoutParams =
            binding.collapsingtoolbarlayout.layoutParams as AppBarLayout.LayoutParams
        toolbarLayoutParams.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        binding.collapsingtoolbarlayout.layoutParams = toolbarLayoutParams
        val appBarLayoutParams = binding.appbar.layoutParams as CoordinatorLayout.LayoutParams
        appBarLayoutParams.behavior = AppBarLayout.Behavior()
        binding.appbar.layoutParams = appBarLayoutParams
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
        val jsonStringPlaylist = GsonBuilder().create().toJson(ListsFragment.musicPlayList)
        sharedPreferencesHelper.savePlaylistData(jsonStringPlaylist)

        ListsFragment.playListAdapter.notifyDataSetChanged()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.MainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.MainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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


