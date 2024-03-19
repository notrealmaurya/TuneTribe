package com.maurya.dtxloopplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.databinding.ActivityFolderBinding
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFolderBinding

    private val viewModel by viewModels<ViewModelObserver>()

    private lateinit var adapterFolder: AdapterFolder

    companion object {
        var folderList: ArrayList<FolderDataClass> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycle.addObserver(viewModel)

        viewModel.fetchFolders(this)


        binding.recyclerViewFolderActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                this@FolderActivity, LinearLayoutManager.VERTICAL, false
            )
            adapterFolder = AdapterFolder(
                this@FolderActivity, folderList
            )
            adapter = adapterFolder
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersStateFLow.collect {
                    binding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            folderList.clear()
                            folderList.addAll(it.data!!)
                            binding.totalFoldersFolderActivity.text = "${folderList.size} folders"
                            adapterFolder.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            showToast(
                                this@FolderActivity,
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


        binding.layoutTopBTN.setOnClickListener {
            finish()
        }

    }


}


