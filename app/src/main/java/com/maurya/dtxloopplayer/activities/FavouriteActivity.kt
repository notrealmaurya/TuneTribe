package com.maurya.dtxloopplayer.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.MainActivity.Companion.favouriteMusicList
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityFavouriteBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.checkListData
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding

    private lateinit var adapterMusic: AdapterMusic

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper


    private var favouriteListPreference: List<MusicDataClass> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferenceHelper = SharedPreferenceHelper(this)


        setRecyclerView()
        fetchSongsFromFavourite()
        changeItemCount()

        binding.shuffleFavouriteActivity.setOnClickListener {
            sendIntent(this, position = 0, reference = "FavouriteActivityShuffle")
        }

        binding.FavouriteBackBtn.setOnClickListener {
            finish()
        }

    }

    private fun setRecyclerView() {
        binding.recyclerViewFavouriteActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(this@FavouriteActivity, LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                this@FavouriteActivity,
                favouriteMusicList,
                sharedPreferenceHelper,
                favouriteActivity = true
            )
            adapter = adapterMusic
        }

    }

    private fun fetchSongsFromFavourite() {
        favouriteListPreference =
            sharedPreferenceHelper.getPlayListSong("myFavouriteYouNoty572noty")

        if (favouriteListPreference.isNotEmpty()) {
            binding.shuffleSongCheckboxLayout.visibility = View.VISIBLE
            binding.recyclerViewFavouriteActivity.visibility = View.VISIBLE
            binding.NoSongsFavouriteActivity.visibility = View.GONE
            favouriteMusicList.clear()
            favouriteMusicList.addAll(favouriteListPreference)
            favouriteMusicList = checkListData(favouriteMusicList)
            adapterMusic.notifyDataSetChanged()
        } else {
            binding.shuffleSongCheckboxLayout.visibility = View.GONE
            binding.recyclerViewFavouriteActivity.visibility = View.GONE
            binding.NoSongsFavouriteActivity.visibility = View.VISIBLE
        }

    }

    private fun changeItemCount() {
        binding.totalSongsFavouriteActivity.text =
            updateTextViewWithItemCount(favouriteMusicList.size)
    }

    override fun onResume() {
        super.onResume()
        adapterMusic.notifyDataSetChanged()
        fetchSongsFromFavourite()
        changeItemCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        favouriteMusicList.clear()
    }


}

