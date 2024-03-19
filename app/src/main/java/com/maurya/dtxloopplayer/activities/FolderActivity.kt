package com.maurya.dtxloopplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.maurya.dtxloopplayer.databinding.ActivityFolderBinding

class FolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        binding.recyclerViewFolderActivity.setHasFixedSize(true)
//        binding.recyclerViewFolderActivity.setItemViewCacheSize(13)
//        binding.recyclerViewFolderActivity.layoutManager = LinearLayoutManager(this)
//        val folderAdapter = FolderAdapter(this, musicFolders)
//        binding.recyclerViewFolderActivity.adapter = folderAdapter
//
//        updateTextViewWithFolderCount(folderAdapter, binding.totalFoldersFolderActivity)
//
//        binding.FolderActivityBackBtn.setOnClickListener { finish() }
    }


}


