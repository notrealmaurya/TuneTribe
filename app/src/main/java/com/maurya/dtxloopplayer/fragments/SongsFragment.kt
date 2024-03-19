package com.maurya.dtxloopplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.databinding.FragmentSongsBinding


class SongsFragment : Fragment() {

    private lateinit var fragmentSongsBinding: FragmentSongsBinding
    private lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    companion object {
        lateinit var musicAdapter: AdapterMusic
        var isInitialized = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentSongsBinding = FragmentSongsBinding.inflate(inflater, container, false)
        val view = fragmentSongsBinding.root

        sharedPreferencesHelper = SharedPreferenceHelper(requireContext())

        isInitialized = true


        fragmentSongsBinding.recyclerViewSongFragment.setHasFixedSize(true)
        fragmentSongsBinding.recyclerViewSongFragment.setItemViewCacheSize(13)
        fragmentSongsBinding.recyclerViewSongFragment.layoutManager =
            LinearLayoutManager(context)
        musicAdapter = AdapterMusic(requireContext(), MainActivity.tempList)
        fragmentSongsBinding.recyclerViewSongFragment.adapter = musicAdapter
        fragmentSongsBinding.MusicListTotalSongFragment.text = "${musicAdapter.itemCount} songs"

        fragmentSongsBinding.shuffleBtnSongFragment.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "SongsFragmentShuffle")
            startActivity(intent)
        }

        fragmentSongsBinding.sortMenuSongFragment.setOnClickListener {
            showSortingMenu(view)
        }

        val sortingOrder = sharedPreferencesHelper.getSortingOrder()
        sortMusicList(sortingOrder.toString())

        return view

    }

    private fun showSortingMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.sortmenu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.newestDatefirst -> {
                    sortMusicList("newest_date_first")
                    true
                }

                R.id.oldestDatefirst -> {
                    sortMusicList("oldest_date_first")
                    true
                }

                R.id.largestSizefirst -> {
                    sortMusicList("largest_size_first")
                    true
                }

                R.id.smallestSizefirst -> {
                    sortMusicList("smallest_size_first")
                    true
                }

                R.id.nameAtoZ -> {
                    sortMusicList("a_to_z")
                    true
                }

                R.id.nameZtoA -> {
                    sortMusicList("z_to_a")
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun sortMusicList(sortBy: String) {
        when (sortBy) {
            "newest_date_first" -> MainActivity.tempList.sortByDescending { it.dateModified }
            "oldest_date_first" -> MainActivity.tempList.sortBy { it.dateModified }
            "largest_size_first" -> MainActivity.tempList.sortByDescending { it.duration }
            "smallest_size_first" -> MainActivity.tempList.sortBy { it.duration }
            "a_to_z" -> MainActivity.tempList.sortBy { it.title }
            "z_to_a" -> MainActivity.tempList.sortByDescending { it.title }
            else -> {
                MainActivity.tempList.sortByDescending { it.dateModified }
            }
        }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder(sortBy)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        musicAdapter.notifyDataSetChanged()
    }

}
