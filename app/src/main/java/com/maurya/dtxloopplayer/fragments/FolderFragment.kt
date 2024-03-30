package com.maurya.dtxloopplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.databinding.FragmentFolderBinding


class FolderFragment : Fragment() {

    private lateinit var fragmentFolderBinding: FragmentFolderBinding

    private lateinit var adapterFolder: AdapterFolder

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
                requireContext(), ListsFragment.folderList, requireActivity().supportFragmentManager
            )
            adapter = adapterFolder
        }

        fragmentFolderBinding.totalFoldersFolderActivity.text = "${adapterFolder.itemCount} folders"


    }


}