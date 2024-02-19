package com.maurya.dtxloopplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.FolderAdapter
import com.maurya.dtxloopplayer.database.FolderData
import com.maurya.dtxloopplayer.database.MusicFolderScanner
import com.maurya.dtxloopplayer.databinding.ActivityFolderBinding
import com.maurya.dtxloopplayer.utils.updateTextViewWithFolderCount

class FolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val musicFolderScanner = MusicFolderScanner(contentResolver)
        val musicFolders = musicFolderScanner.getAllMusicFolders()


        binding.recyclerViewFolderActivity.setHasFixedSize(true)
        binding.recyclerViewFolderActivity.setItemViewCacheSize(13)
        binding.recyclerViewFolderActivity.layoutManager = LinearLayoutManager(this)
        val folderAdapter = FolderAdapter(this, musicFolders)
        binding.recyclerViewFolderActivity.adapter = folderAdapter

        updateTextViewWithFolderCount(folderAdapter, binding.totalFoldersFolderActivity)

        binding.FolderActivityBackBtn.setOnClickListener { finish() }
    }


}


