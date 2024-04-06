package com.maurya.dtxloopplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.FragmentFavouriteBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavouriteFragment : Fragment(), MediaControlInterface {

    private lateinit var fragmentFavouriteBinding: FragmentFavouriteBinding


    private val viewModel: ViewModelObserver by viewModels()

    private lateinit var adapterMusic: AdapterMusic

    companion object {

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFavouriteBinding = FragmentFavouriteBinding.inflate(inflater, container, false)
        val view = fragmentFavouriteBinding.root

        view.setOnTouchListener { _, _ -> true }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainBinding = MainActivity.getActivityMainBinding()
        mainBinding?.topLayout?.visibility = View.GONE

        setRecyclerView()
        fetchSongsFromFavourite()
        changeItemCount()

        fragmentFavouriteBinding.shuffleFavouriteActivity.setOnClickListener {
            if (activity is MainActivity) {
                (activity as MainActivity).onSongShuffled(
                    MainActivity.favouriteMusicList,
                    true
                )
            }
        }

        fragmentFavouriteBinding.FavouriteBackBtn.setOnClickListener {
            mainBinding?.topLayout?.visibility = View.VISIBLE
            requireActivity().onBackPressed()
        }


    }

    private fun setRecyclerView() {
        fragmentFavouriteBinding.recyclerViewFavouriteActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                requireContext(),
                MainActivity.favouriteMusicList,
                this@FavouriteFragment,
                favouriteActivity = true
            )
            adapter = adapterMusic
        }

    }

    private fun fetchSongsFromFavourite() {

        if (MainActivity.favouriteMusicList.isNotEmpty()) {
            fragmentFavouriteBinding.recyclerViewFavouriteActivity.visibility = View.VISIBLE
            fragmentFavouriteBinding.NoSongsFavouriteActivity.visibility = View.GONE
        } else {
            fragmentFavouriteBinding.recyclerViewFavouriteActivity.visibility = View.GONE
            fragmentFavouriteBinding.NoSongsFavouriteActivity.visibility = View.VISIBLE
        }

    }

    private fun changeItemCount() {
        fragmentFavouriteBinding.totalSongsFavouriteActivity.text =
            updateTextViewWithItemCount(MainActivity.favouriteMusicList.size)
    }

    override fun onResume() {
        super.onResume()
        adapterMusic.notifyDataSetChanged()
        fetchSongsFromFavourite()
        changeItemCount()
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