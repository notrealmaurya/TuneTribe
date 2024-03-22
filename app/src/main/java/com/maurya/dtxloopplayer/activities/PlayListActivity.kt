package com.maurya.dtxloopplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.databinding.ActivityPlaylistBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class PlayListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding

    private lateinit var adapterMusic: AdapterMusic

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    companion object {
        var currentPlayListMusicList: ArrayList<MusicDataClass> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        val currentPlayListPosition = intent.getIntExtra("index", -1)
        val currentPlayListUUID = intent.getStringExtra("uuid")

        val playListSongPreference =
            sharedPreferenceHelper.getPlayListSong(currentPlayListUUID.toString())

        if (playListSongPreference.isNotEmpty()){
            currentPlayListMusicList.clear()
            currentPlayListMusicList.addAll(playListSongPreference)
        }

        Log.d("playItemClass", currentPlayListMusicList.toString())


        fetchSongsFromPlayList()

        listeners(currentPlayListPosition, currentPlayListUUID.toString())

    }

    private fun fetchSongsFromPlayList() {
        binding.recyclerViewPlayListActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(this@PlayListActivity, LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                this@PlayListActivity,
                currentPlayListMusicList,
                sharedPreferenceHelper,
                playListActivity = true
            )
            adapter = adapterMusic
        }
    }

    private fun listeners(currentPlayListPosition: Int, currentPlayListUUID: String) {

        changeItemCount()

        binding.playlistNamePlayListActivity.text =
            ListsFragment.playList[currentPlayListPosition].playListName

        binding.PlayListBackBtn.setOnClickListener { finish() }

        binding.addBtnPlayListActivity.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            intent.putExtra("uuid", currentPlayListUUID)
            startActivity(intent)
        }

        binding.shuffleBtnPlayListActivity.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlayListActivityShuffle")
            startActivity(intent)
        }

        binding.removeAllBtnPlayListActivity.setOnClickListener {

            val alertDialog = MaterialAlertDialogBuilder(this)
            alertDialog.setTitle("Remove all")
                .setMessage("Are you sure you want to remove all songs from this playlist?")
                .setPositiveButton("OK") { dialog, _ ->
                    currentPlayListMusicList.clear()
                    sharedPreferenceHelper.savePlayListSong(
                        currentPlayListMusicList,
                        currentPlayListUUID
                    )
//                    sharedPreferenceHelper.savePlayListSongCount(
//                        currentPlayListMusicList.size,
//                        currentPlayListUUID
//                    )
                    adapterMusic.notifyDataSetChanged()
                    changeItemCount()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }

    }


    private fun changeItemCount() {
        binding.totalSongsPlayListActivity.text =
            updateTextViewWithItemCount(currentPlayListMusicList.size)
    }

    override fun onResume() {
        super.onResume()
        adapterMusic.notifyDataSetChanged()
        changeItemCount()
    }


}