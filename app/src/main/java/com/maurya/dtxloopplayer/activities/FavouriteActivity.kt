package com.maurya.dtxloopplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityFavouriteBinding
import com.maurya.dtxloopplayer.utils.checkPlayListData
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding

    private lateinit var musicAdapter: AdapterMusic

    companion object {
        var favouriteSongs: ArrayList<MusicDataClass> = ArrayList()
        var favouritesChanged: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favouriteSongs = checkPlayListData(favouriteSongs)

        binding.FavouriteBackBtn.setOnClickListener {
            finish()
        }

        binding.recyclerViewFavouriteActivity.setHasFixedSize(true)
        binding.recyclerViewFavouriteActivity.setItemViewCacheSize(13)
        binding.recyclerViewFavouriteActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter = AdapterMusic(this, favouriteSongs, favouriteActivity = true)
        binding.recyclerViewFavouriteActivity.adapter = musicAdapter

        itemCount()

        favouritesChanged = false

        if (favouriteSongs.size >= 1) {
            binding.shuffleSongCheckboxLayout.visibility = View.VISIBLE
            binding.recyclerViewFavouriteActivity.visibility = View.VISIBLE
            binding.NoSongsFavouriteActivity.visibility = View.INVISIBLE
        }

        binding.shuffleFavouriteActivity.setOnClickListener {
            sendIntent(this, reference = "FavouriteActivityShuffle", position = 0)
        }

        itemCount()
    }

    override fun onResume() {
        super.onResume()
        if (favouritesChanged) {
            musicAdapter.updateFavourites(favouriteSongs)
            favouritesChanged = false
        }
        itemCount()
    }

    private fun itemCount() {
        val count = updateTextViewWithItemCount(musicAdapter.itemCount)
        binding.totalSongsFavouriteActivity.text = count
    }


}

