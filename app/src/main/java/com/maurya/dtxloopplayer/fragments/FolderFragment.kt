package com.maurya.dtxloopplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.databinding.FragmentFolderBinding
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderFragment : Fragment() {

    private lateinit var fragmentFolderBinding: FragmentFolderBinding
    private val viewModel: ViewModelObserver by viewModels()

    private lateinit var adapterFolder: AdapterFolder

    private lateinit var folderAdapter: AdapterFolder

    companion object {
        var folderList: ArrayList<FolderDataClass> = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFolderBinding = FragmentFolderBinding.inflate(inflater, container, false)
        val view = fragmentFolderBinding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentFolderBinding.recyclerViewFolderActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
            adapterFolder = AdapterFolder(
                requireContext(), folderList, requireActivity().supportFragmentManager
            )
            adapter = adapterFolder
        }

        fragmentFolderBinding.totalFoldersFolderActivity.text = "${adapterFolder.itemCount} folders"

        fetchFolderUsingViewModel()

    }


    private fun fetchFolderUsingViewModel() {

        viewModel.fetchFolders(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersStateFLow.collect {
                    when (it) {
                        is ModelResult.Success -> {
                            fragmentFolderBinding.progressBar.visibility = View.GONE
                            folderList.addAll(it.data!!)
                        }

                        is ModelResult.Error -> {
                            showToast(
                                requireContext(),
                                it.message.toString()
                            )
                        }

                        is ModelResult.Loading -> {
                            folderList.clear()
                            fragmentFolderBinding.progressBar.visibility = View.VISIBLE
                            adapterFolder.notifyDataSetChanged()
                        }

                        else -> {}
                    }
                }
            }
        }

    }


}