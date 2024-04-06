package com.maurya.dtxloopplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.FragmentSearchBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(), MediaControlInterface {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var adapterMusic: AdapterMusic

    companion object {
        lateinit var musicListSearch: ArrayList<MusicDataClass>
        var isSearchViewOpen: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        view.setOnTouchListener { _, _ -> true }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        musicListSearch = ArrayList()

        val mainBinding = MainActivity.getActivityMainBinding()
        mainBinding?.topLayout?.visibility = View.GONE

        binding.SearchMusicViewSearchActivity.onActionViewExpanded()
        binding.SearchMusicViewSearchActivity.requestFocus()

        binding.SearchActivityBackBtn.setOnClickListener {
            mainBinding?.topLayout?.visibility = View.VISIBLE
            requireActivity().onBackPressed()
        }

        adapterMusic = AdapterMusic(
            requireContext(),
            musicListSearch, this@SearchFragment, searchActivity = true
        )

        binding.recyclerViewSearchActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adapterMusic
        }



        binding.SearchMusicViewSearchActivity.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch.clear()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in SongsFragment.musicList) {
                        if (song.musicName.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    }
                    isSearchViewOpen = true
                    adapterMusic.updateSearchList(searchList = musicListSearch)
                }
                return true
            }
        })


    }


    override fun onSongSelected(
        musicList: ArrayList<MusicDataClass>,
        position: Int
    ) {
        if (activity is MainActivity) {
            (activity as MainActivity).onSongSelected(musicList, position)
        }
    }


    override fun onSongShuffled(musicList: ArrayList<MusicDataClass>, shuffle: Boolean) {


    }

}