package com.maurya.dtxloopplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.SelectionActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.FragmentFavouriteBinding
import com.maurya.dtxloopplayer.databinding.FragmentPlayListBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.checkListData
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.sortMusicList
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class PlayListFragment : Fragment(), MediaControlInterface {


    private lateinit var binding: FragmentPlayListBinding

    private lateinit var adapterMusic: AdapterMusic

    private val viewModel: ViewModelObserver by viewModels()

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    companion object {
        var currentPlayListMusicList: ArrayList<MusicDataClass> = arrayListOf()
    }

    private var currentPlayListName: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayListBinding.inflate(inflater, container, false)
        val view = binding.root

        view.setOnTouchListener { _, _ -> true }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferenceHelper = SharedPreferenceHelper(requireContext())

        val mainBinding = MainActivity.getActivityMainBinding()
        mainBinding?.topLayout?.visibility = View.GONE

        binding.PlayListBackBtn.setOnClickListener {
            mainBinding?.topLayout?.visibility = View.VISIBLE
            requireActivity().onBackPressed()
        }

        val bundle = arguments
        currentPlayListName = bundle?.getString("playListName").toString()

        viewModel.fetchSongsFromPlayList(
            requireContext(),
            currentPlayListName.toString(),
            sharedPreferenceHelper
        )

        setRecyclerView()
        fetchSongsFromPlayList()
        listeners(currentPlayListName.toString())

    }


    private fun fetchSongsFromPlayList() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songFromPlayListStateFLow.collect {
                    when (it) {
                        is ModelResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            currentPlayListMusicList.clear()
                            currentPlayListMusicList.addAll(it.data!!)
                            changeItemCount()
                            if (currentPlayListMusicList.isNotEmpty()) {
                                binding.noSongsPlaylistActivity.visibility = View.GONE
                                binding.recyclerViewPlayListActivity.visibility = View.VISIBLE
                                binding.shuffle.visibility = View.VISIBLE
                                currentPlayListMusicList = checkListData(currentPlayListMusicList)
                                adapterMusic.notifyDataSetChanged()
                            } else {
                                binding.recyclerViewPlayListActivity.visibility = View.GONE
                                binding.shuffle.visibility = View.GONE
                                binding.noSongsPlaylistActivity.visibility = View.VISIBLE
                            }
                        }

                        is ModelResult.Error -> {
                            binding.progressBar.visibility = View.GONE
                            showToast(requireContext(), it.message.toString())
                        }

                        is ModelResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }

        }

    }

    private fun setRecyclerView() {
        binding.recyclerViewPlayListActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                requireContext(),
                currentPlayListMusicList,
                this@PlayListFragment,
                sharedPreferenceHelper,
                playListActivity = true
            )
            adapter = adapterMusic
        }
    }

    private fun listeners(currentPlayListUUID: String) {

        changeItemCount()

        binding.playlistNamePlayListActivity.text = currentPlayListUUID


        binding.addBtnPlayListActivity.setOnClickListener {
            val intent = Intent(requireContext(), SelectionActivity::class.java)
            intent.putExtra("playListName", currentPlayListUUID)
            startActivity(intent)
        }

        binding.shuffleBtnPlayListActivity.setOnClickListener {
            if (activity is MainActivity) {
                (activity as MainActivity).onSongShuffled(currentPlayListMusicList, true)
            }
        }

        binding.removeAllBtnPlayListActivity.setOnClickListener {

            val alertDialog = MaterialAlertDialogBuilder(requireContext())
            alertDialog.setTitle("Remove all")
                .setMessage("Are you sure you want to remove all songs from this playlist?")
                .setPositiveButton("OK") { dialog, _ ->
                    currentPlayListMusicList.clear()
                    sharedPreferenceHelper.savePlayListSong(
                        currentPlayListMusicList,
                        currentPlayListUUID
                    )
                    sharedPreferenceHelper.savePlayListSongCount(
                        currentPlayListMusicList.size,
                        currentPlayListUUID
                    )
                    adapterMusic.notifyDataSetChanged()
                    changeItemCount()
                    fetchSongsFromPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }

    }

    private fun changeItemCount() {
        binding.totalSongsPlayListActivity.text =
            updateTextViewWithItemCount(currentPlayListMusicList.size)
    }
    

    override fun onDestroy() {
        super.onDestroy()
        currentPlayListMusicList.clear()
    }

    override fun onSongSelected(musicList: ArrayList<MusicDataClass>, position: Int) {


    }

    override fun onSongShuffled(musicList: ArrayList<MusicDataClass>, shuffle: Boolean) {


    }

    override fun onAddToQueue(song: MusicDataClass) {

    }
}