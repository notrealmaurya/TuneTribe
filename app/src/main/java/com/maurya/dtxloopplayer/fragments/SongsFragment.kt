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
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.dataEntity.MusicData
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.databinding.FragmentSongsBinding


class SongsFragment : Fragment() {

    private lateinit var fragmentSongsBinding: FragmentSongsBinding
//    private val musicViewModel by activityViewModels<MusicViewModel>()
    private lateinit var sharedPreferencesHelper: SharedPreferenceHelper


    companion object {
        lateinit var musicListSongFragment: ArrayList<MusicData>
        lateinit var musicAdapter: MusicAdapter
        var isInitialized = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSongsBinding = FragmentSongsBinding.inflate(inflater, container, false)
        val view = fragmentSongsBinding.root

        sharedPreferencesHelper = SharedPreferenceHelper(requireContext())

        isInitialized = true


        fragmentSongsBinding.recyclerViewSongFragment.setHasFixedSize(true)
        fragmentSongsBinding.recyclerViewSongFragment.setItemViewCacheSize(13)
        fragmentSongsBinding.recyclerViewSongFragment.layoutManager =
            LinearLayoutManager(context)
        musicAdapter = MusicAdapter(requireContext(), MainActivity.tempList)
        fragmentSongsBinding.recyclerViewSongFragment.adapter = musicAdapter
        fragmentSongsBinding.MusicListTotalSongFragment.text = "${musicAdapter.itemCount} songs"


        fragmentSongsBinding.shuffleBtnSongFragment.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "SongsFragmentShuffle")
            startActivity(intent)
        }

        fragmentSongsBinding.sortMenuSongFragment.setOnClickListener { view ->
            showSortingMenu(view)
        }

        val sortingOrder = sharedPreferencesHelper.getSortingOrder()
        // Apply the saved sorting order to your music list if it exists
        when (sortingOrder) {
            "newest_date_first" -> sortMusicListByNewestDateFirst()
            "oldest_date_first" -> sortMusicListByOldestDateFirst()
            "largest_size_first" -> sortMusicListByLargestSizeFirst()
            "smallest_size_first" -> sortMusicListBySmallestSizeFirst()
            "a_to_z" -> sortMusicListByNameAtoZ()
            "z_to_a" -> sortMusicListByNameZtoA()
            else -> {
                sortMusicListByNewestDateFirst()
            }
        }

        return view

    }

    private fun showSortingMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.sortmenu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.newestDatefirst -> {
                    sortMusicListByNewestDateFirst()
                    true
                }

                R.id.oldestDatefirst -> {
                    sortMusicListByOldestDateFirst()
                    true
                }

                R.id.largestSizefirst -> {
                    sortMusicListByLargestSizeFirst()
                    true
                }

                R.id.smallestSizefirst -> {
                    sortMusicListBySmallestSizeFirst()
                    true
                }

                R.id.nameAtoZ -> {
                    sortMusicListByNameAtoZ()
                    true
                }

                R.id.nameZtoA -> {
                    sortMusicListByNameZtoA()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    // Sort music list by newest date first
    private fun sortMusicListByNewestDateFirst() {
        MainActivity.tempList.sortByDescending { it.dateModified }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("newest_date_first")
    }

    // Sort music list by oldest date first
    private fun sortMusicListByOldestDateFirst() {
        MainActivity.tempList.sortBy { it.dateModified }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("oldest_date_first")
    }

    // Sort music list by largest size first
    private fun sortMusicListByLargestSizeFirst() {
        MainActivity.tempList.sortByDescending { it.duration }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("largest_size_first")
    }

    // Sort music list by smallest size first
    private fun sortMusicListBySmallestSizeFirst() {
        MainActivity.tempList.sortBy { it.duration }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("smallest_size_first")
    }

    // Sort music list by name (A to Z)
    private fun sortMusicListByNameAtoZ() {
        MainActivity.tempList.sortBy { it.title }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("a_to_z")
    }

    // Sort music list by name (Z to A)
    private fun sortMusicListByNameZtoA() {
        MainActivity.tempList.sortByDescending { it.title }
        musicAdapter.updateMusicList(MainActivity.tempList)
        musicAdapter.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder("z_to_a")
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
