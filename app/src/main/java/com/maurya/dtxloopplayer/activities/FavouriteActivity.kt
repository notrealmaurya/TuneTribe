package com.maurya.dtxloopplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.databinding.ActivityFavouriteBinding
import com.maurya.dtxloopplayer.utils.checkPlayListData

class FavouriteActivity : AppCompatActivity() {

    companion object {
        var favouriteSongs: ArrayList<MusicData> = ArrayList()
        var favouritesChanged: Boolean = false
        lateinit var binding: ActivityFavouriteBinding
        lateinit var musicAdapter: MusicAdapter


        var isInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isInitialized = true
        //check file is present or not
        favouriteSongs = checkPlayListData(favouriteSongs)

        binding.FavouriteBackBtn.setOnClickListener {
            finish()
        }

        binding.recyclerViewFavouriteActivity.setHasFixedSize(true)
        binding.recyclerViewFavouriteActivity.setItemViewCacheSize(13)
        binding.recyclerViewFavouriteActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this, favouriteSongs, favouriteActivity = true)
        binding.recyclerViewFavouriteActivity.adapter = musicAdapter

        itemCount()

        favouritesChanged = false

        if (favouriteSongs.size >= 1) {
            binding.shuffleSongCheckboxLayout.visibility = View.VISIBLE
            binding.recyclerViewFavouriteActivity.visibility = View.VISIBLE
            binding.NoSongsFavouriteActivity.visibility = View.INVISIBLE
        }
        binding.shuffleFavouriteActivity.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteActivityShuffle")
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if (favouritesChanged) {
            musicAdapter.updateFavourites(favouriteSongs)
            favouritesChanged = false
        }
        itemCount()
    }

    private fun itemCount(){
        val itemCount = musicAdapter.itemCount
        if (itemCount == 1 || itemCount == 0) {
            binding.totalSongsFavouriteActivity.text = "${itemCount} song"
        } else {
            binding.totalSongsFavouriteActivity.text = "${itemCount} songs"
        }
    }


}

