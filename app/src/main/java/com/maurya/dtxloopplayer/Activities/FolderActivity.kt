package com.maurya.dtxloopplayer.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.ListFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.Adapter.FolderAdapter
import com.maurya.dtxloopplayer.FolderData
import com.maurya.dtxloopplayer.Fragments.ListsFragment
import com.maurya.dtxloopplayer.MusicFolderScanner
import com.maurya.dtxloopplayer.databinding.ActivityFolderBinding
import com.maurya.dtxloopplayer.updateTextViewWithFolderCount

class FolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderBinding
    private var folders = ArrayList<FolderData>()


    companion object {
        var search: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usage in your activity or fragment
        val musicFolderScanner = MusicFolderScanner(contentResolver)
        val musicFolders = musicFolderScanner.getAllMusicFolders()


        // Set up the RecyclerView with the adapter
        binding.recyclerViewFolderActivity.setHasFixedSize(true)
        binding.recyclerViewFolderActivity.setItemViewCacheSize(13)
        binding.recyclerViewFolderActivity.layoutManager = LinearLayoutManager(this)
        val folderAdapter = FolderAdapter(this, musicFolders)
        binding.recyclerViewFolderActivity.adapter = folderAdapter

        updateTextViewWithFolderCount(folderAdapter, binding.totalFoldersFolderActivity)


        binding.FolderActivityBackBtn.setOnClickListener { finish() }
    }





}

