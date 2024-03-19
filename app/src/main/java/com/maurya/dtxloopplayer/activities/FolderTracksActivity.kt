package com.maurya.dtxloopplayer.activities

import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.databinding.ActivityFolderTracksActiivityBinding
import java.io.File
import android.util.Base64
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.utils.getAllFolders
import com.maurya.dtxloopplayer.utils.getSongsFromFolderPath
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import java.util.Locale


class FolderTracksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderTracksActiivityBinding

    companion object {
        var folderMusicFiles = ArrayList<MusicDataClass>()
        lateinit var musicAdapter: AdapterMusic
        var isInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderTracksActiivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isInitialized = true
        val folderPath = intent.getStringExtra("folderPath") ?: ""
//        folderMusicFiles = getSongsFromFolderPath(this@FolderTracksActivity, folderPath)


        binding.recyclerViewFoldersTrackActivity.setItemViewCacheSize(10)
        binding.recyclerViewFoldersTrackActivity.setHasFixedSize(true)
        binding.recyclerViewFoldersTrackActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter =
            AdapterMusic(this, folderMusicFiles, folderSongsActivity = true)
        binding.recyclerViewFoldersTrackActivity.adapter = musicAdapter

        updateTextViewWithItemCount(musicAdapter, binding.totalSongsFoldersTrackActivity)

        val folderName = File(folderPath).name
        binding.foldersNameFoldersTrackActivity.text = folderName


        listeners()

    }

    private fun listeners() {

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