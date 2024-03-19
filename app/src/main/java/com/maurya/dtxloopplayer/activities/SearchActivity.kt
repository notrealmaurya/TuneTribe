package com.maurya.dtxloopplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivitySearchBinding
import com.maurya.dtxloopplayer.fragments.SongsFragment

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding


    companion object {
        lateinit var musicListSearch: ArrayList<MusicDataClass>
        var search: Boolean = false
        var isInitialized = false
        lateinit var musicAdapter: AdapterMusic
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isInitialized=true

        binding.SearchMusicViewSearchActivity.onActionViewExpanded()

        binding.SearchActivityBackBtn.setOnClickListener {
            finish()
        }


        binding.recyclerViewSearchActivity.setHasFixedSize(true)
        binding.recyclerViewSearchActivity.setItemViewCacheSize(13)
        binding.recyclerViewSearchActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter = AdapterMusic(this, SongsFragment.musicList, searchActivity = true)
        binding.recyclerViewSearchActivity.adapter = musicAdapter



        binding.SearchMusicViewSearchActivity.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in SongsFragment.musicList) {
                        if (song.musicName.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    }
                    search = true
                    musicAdapter.updateSearchList(searchList = musicListSearch)
                    binding.MusicListTotalSongFragment.text = "${musicAdapter.itemCount} songs"
                }
                return true
            }
        })


    }


}