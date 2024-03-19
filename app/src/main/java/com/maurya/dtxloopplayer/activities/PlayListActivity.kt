package com.maurya.dtxloopplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.utils.checkPlayListData
import com.maurya.dtxloopplayer.databinding.ActivityPlaylistBinding
import com.maurya.dtxloopplayer.utils.showToast

class PlayListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    companion object {
        var currentPlayListPosition: Int = -1
        var isInitialized = false
        lateinit var musicAdapter: AdapterMusic
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isInitialized = true
        currentPlayListPosition = intent.extras?.get("index") as Int

        try {
            val currentPlayList = ListsFragment.musicPlayList.ref[currentPlayListPosition]
            currentPlayList.playList = checkPlayListData(playList = currentPlayList.playList)
        }
        catch (e :Exception){
            showToast(this,"Playlist Error")
        }

        binding.recyclerViewPlayListActivity.setItemViewCacheSize(10)
        binding.recyclerViewPlayListActivity.setHasFixedSize(true)
        binding.recyclerViewPlayListActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter = AdapterMusic(
            this,
            ListsFragment.musicPlayList.ref[currentPlayListPosition].playList,
            playListActivity = true
        )
        binding.recyclerViewPlayListActivity.adapter = musicAdapter


        listeners()

    }

    private fun listeners() {

        binding.PlayListBackBtn.setOnClickListener {
            finish()
        }

        binding.addBtnPlayListActivity.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        binding.shuffleBtnPlayListActivity.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlayListActivityShuffle")
            startActivity(intent)
        }

        binding.addBtnPlayListActivity.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        binding.removeAllBtnPlayListActivity.setOnClickListener {

            val alertDialog = MaterialAlertDialogBuilder(this)
            alertDialog.setTitle("Remove all")
                .setMessage("Are you sure you want to remove all songs from this playlist?")
                .setPositiveButton("OK") { dialog, _ ->
                    ListsFragment.musicPlayList.ref[currentPlayListPosition].playList.clear()
                    musicAdapter.refreshPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }


    }



    override fun onResume() {
        super.onResume()
        binding.playlistNamePlayListActivity.text =
            ListsFragment.musicPlayList.ref[currentPlayListPosition].name
        binding.totalSongsPlayListActivity.text = "${musicAdapter.itemCount} songs"


        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(ListsFragment.musicPlayList)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()

        musicAdapter.notifyDataSetChanged()
    }

    override fun onDestroy(){
        super.onDestroy()
        binding.playlistNamePlayListActivity.text =
            ListsFragment.musicPlayList.ref[currentPlayListPosition].name
        binding.totalSongsPlayListActivity.text = "${musicAdapter.itemCount} songs"


        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(ListsFragment.musicPlayList)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()

        musicAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        binding.playlistNamePlayListActivity.text =
            ListsFragment.musicPlayList.ref[currentPlayListPosition].name
        binding.totalSongsPlayListActivity.text = "${musicAdapter.itemCount} songs"


        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(ListsFragment.musicPlayList)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()

        musicAdapter.notifyDataSetChanged()
    }


}