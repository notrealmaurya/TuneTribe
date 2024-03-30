package com.maurya.dtxloopplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivitySearchBinding
import com.maurya.dtxloopplayer.fragments.SongsFragment
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity(), MediaControlInterface {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var adapterMusic: AdapterMusic

    companion object {
        lateinit var musicListSearch: ArrayList<MusicDataClass>
        var search: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.SearchMusicViewSearchActivity.onActionViewExpanded()

        binding.SearchActivityBackBtn.setOnClickListener {
            finish()
        }

        binding.recyclerViewSearchActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                this@SearchActivity,
                SongsFragment.musicList, this@SearchActivity, searchActivity = true
            )
            adapter = adapterMusic
        }


        binding.MusicListTotalSearchActivity.text =
            updateTextViewWithItemCount(SongsFragment.musicList.size)

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
                    adapterMusic.updateSearchList(searchList = musicListSearch)
                    binding.MusicListTotalSearchActivity.text =
                        updateTextViewWithItemCount(adapterMusic.itemCount)
                }
                return true
            }
        })


    }


    override fun onSongSelected(
        musicList: ArrayList<MusicDataClass>,
        position: Int
    ) {
        TODO("Not yet implemented")
    }


    override fun onAddToQueue(song: MusicDataClass) {
        TODO("Not yet implemented")
    }


}