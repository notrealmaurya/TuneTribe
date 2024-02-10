package com.maurya.dtxloopplayer.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.dataEntity.MusicData
import com.maurya.dtxloopplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    private lateinit var adapter: MusicAdapter
    private val musicList = mutableListOf<MusicData>() // Your list of songs

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivitySelectionBinding
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.recyclerViewSelectionActivity.setHasFixedSize(true)
        binding.recyclerViewSelectionActivity.setItemViewCacheSize(25)
        binding.recyclerViewSelectionActivity.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.tempList, selectionActivity = true)
        binding.recyclerViewSelectionActivity.adapter = adapter



        listeners()

    }


    private fun listeners() {


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
                    for (song in MainActivity.tempList) {
                        if (song.title.lowercase().contains(userInput))
                            SearchActivity.musicListSearch.add(song)
                    }
                    SearchActivity.search = true
                    adapter.updateMusicList(searchList = SearchActivity.musicListSearch)
                }
                return true
            }
        })




    }

}