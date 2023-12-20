package com.maurya.dtxloopplayer.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.Adapter.FavouriteAdapter
import com.maurya.dtxloopplayer.Adapter.MusicAdapter
import com.maurya.dtxloopplayer.MusicData
import com.maurya.dtxloopplayer.checkPlayListData
import com.maurya.dtxloopplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    companion object {
        var favouriteSongs: ArrayList<MusicData> = ArrayList()
        var favouritesChanged: Boolean = false
        lateinit var binding: ActivityFavouriteBinding
        lateinit var favouriteAdapter: FavouriteAdapter


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
        favouriteAdapter = FavouriteAdapter(this, favouriteSongs)
        binding.recyclerViewFavouriteActivity.adapter = favouriteAdapter


        val itemCount = favouriteAdapter.itemCount
        if (itemCount == 1 || itemCount == 0) {
            binding.totalSongsFavouriteActivity.text = "${itemCount} song"
        } else {
            binding.totalSongsFavouriteActivity.text = "${itemCount} songs"
        }

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
            favouriteAdapter.updateFavourites(favouriteSongs)
            favouritesChanged = false
        }

        val itemCount = favouriteAdapter.itemCount
        if (itemCount == 1 || itemCount == 0) {
            binding.totalSongsFavouriteActivity.text = "${itemCount} song"
        } else {
            binding.totalSongsFavouriteActivity.text = "${itemCount} songs"
        }

    }



}

