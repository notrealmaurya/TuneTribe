package com.maurya.dtxloopplayer.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.FragmentFolderBinding
import com.maurya.dtxloopplayer.databinding.FragmentFolderTracksBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import kotlinx.coroutines.launch
import java.io.File


class FolderTracksFragment : Fragment(), MediaControlInterface {

    private lateinit var fragmentFolderTracksBinding: FragmentFolderTracksBinding


    private val viewModel by viewModels<ViewModelObserver>()
    private lateinit var adapterMusic: AdapterMusic

    companion object {
        var folderMusicList = ArrayList<MusicDataClass>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFolderTracksBinding =
            FragmentFolderTracksBinding.inflate(inflater, container, false)
        val view = fragmentFolderTracksBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments

        if (bundle != null) {
            val folderPath = bundle.getString("folderPath")
            viewModel.fetchSongsFromFolder(requireContext(), folderPath!!)
            fragmentFolderTracksBinding.foldersNameFoldersTrackActivity.text = File(folderPath).name
        }
        else{
            showToast(requireContext(),"Error in fetching MusicFiles")
        }

        fragmentFolderTracksBinding.recyclerViewFoldersTrackActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
            adapterMusic = AdapterMusic(
                requireContext(),
                folderMusicList,
                this@FolderTracksFragment,
                folderSongsActivity = true
            )
            adapter = adapterMusic
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songFromFoldersStateFLow.collect {
                    fragmentFolderTracksBinding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            folderMusicList.clear()
                            folderMusicList.addAll(it.data!!)
                            val count = updateTextViewWithItemCount(folderMusicList.size)
                            fragmentFolderTracksBinding.totalSongsFoldersTrackActivity.text = count
                            adapterMusic.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            showToast(
                                requireContext(),
                                it.message.toString()
                            )
                        }

                        is ModelResult.Loading -> {
                            fragmentFolderTracksBinding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }

        fragmentFolderTracksBinding.folderTracksBackBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        fragmentFolderTracksBinding.shuffleBtnFoldersTrackActivity.setOnClickListener {
//            val intent = Intent(this, PlayerActivity::class.java)
//            intent.putExtra("index", 0)
//            intent.putExtra("class", "folderSongsActivityShuffle")
//            startActivity(intent)
        }

    }

    override fun onSongSelected(musicList: ArrayList<MusicDataClass>, position: Int) {


    }

    override fun onAddToQueue(song: MusicDataClass) {
        TODO("Not yet implemented")
    }


}