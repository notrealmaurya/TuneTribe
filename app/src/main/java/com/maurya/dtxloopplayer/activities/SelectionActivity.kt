package com.maurya.dtxloopplayer.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivitySelectionBinding
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SelectionActivity : AppCompatActivity() {

    private lateinit var adapterMusic: AdapterMusic

    private lateinit var binding: ActivitySelectionBinding

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper


    companion object {
        var selectionCount: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferenceHelper = SharedPreferenceHelper(this)


        val currentPlayListUUID = intent.getStringExtra("playListName")

        binding.recyclerViewSelectionActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(this@SelectionActivity, LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                this@SelectionActivity,
                SongsFragment.musicList,
                null,
                sharedPreferenceHelper,
                selectionActivity = true,
                uuidCurrentPlayList = currentPlayListUUID.toString()
            )
            adapter = adapterMusic
        }

        listeners()

    }


    private fun listeners() {

        binding.selectedSongSelectionActivity.text =
            "${updateTextViewWithItemCount(selectionCount)} selected"


        //backBtn
        binding.backBtnSelectionActivity.setOnClickListener {
            finish()
        }
        //SearchView
        binding.searchMusicViewSelectionActivity.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                SearchActivity.musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in SongsFragment.musicList) {
                        if (song.musicName.lowercase().contains(userInput))
                            SearchActivity.musicListSearch.add(song)
                    }
                    SearchActivity.search = true
                    adapterMusic.updateSearchList(searchList = SearchActivity.musicListSearch)
                }
                return true
            }
        })


    }

}