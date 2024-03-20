package com.maurya.dtxloopplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.databinding.ActivityFolderTracksActiivityBinding
import java.io.File
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderTracksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderTracksActiivityBinding

    private val viewModel by viewModels<ViewModelObserver>()
    private lateinit var adapterMusic: AdapterMusic


    companion object {
        var folderMusicList = ArrayList<MusicDataClass>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderTracksActiivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderPath = intent.getStringExtra("folderPath") ?: ""

        lifecycle.addObserver(viewModel)

        viewModel.fetchSongsFromFolder(this, folderPath)


        binding.recyclerViewFoldersTrackActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                this@FolderTracksActivity, LinearLayoutManager.VERTICAL, false
            )
            adapterMusic = AdapterMusic(
                this@FolderTracksActivity, folderMusicList, folderSongsActivity = true
            )
            adapter = adapterMusic
        }

        binding.foldersNameFoldersTrackActivity.text = File(folderPath).name

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songFromFoldersStateFLow.collect {
                    binding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            folderMusicList.clear()
                            folderMusicList.addAll(it.data!!)
                            val count = updateTextViewWithItemCount(folderMusicList.size)
                            binding.totalSongsFoldersTrackActivity.text = count
                            adapterMusic.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            showToast(
                                this@FolderTracksActivity,
                                it.message.toString()
                            )
                        }

                        is ModelResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }

        binding.folderTracksBackBtn.setOnClickListener {
            finish()
        }

        binding.shuffleBtnFoldersTrackActivity.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "folderSongsActivityShuffle")
            startActivity(intent)
        }

    }


}