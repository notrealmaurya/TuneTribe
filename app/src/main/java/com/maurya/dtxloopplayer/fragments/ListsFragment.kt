package com.maurya.dtxloopplayer.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.FolderActivity
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.adapter.AdapterPlayList
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.database.PlayListDataClass
import com.maurya.dtxloopplayer.databinding.FragmentListsBinding
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ListsFragment : Fragment() {

    private lateinit var folderAdapter: AdapterFolder


    private val viewModel: ViewModelObserver by viewModels()

    private lateinit var fragmentListsBinding: FragmentListsBinding

    private lateinit var adapterPlayList: AdapterPlayList

    companion object {
        var playList: ArrayList<PlayListDataClass> = arrayListOf()
        var folderList: ArrayList<FolderDataClass> = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentListsBinding = FragmentListsBinding.inflate(inflater, container, false)
        val view = fragmentListsBinding.root


        lifecycle.addObserver(viewModel)

        fetchFolderUsingViewModel()
        fetchPlayListUsingViewModel()

        listeners()



        return view
    }

    private fun fetchPlayListUsingViewModel() {

        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterPlayList = AdapterPlayList(
                requireContext(),
                playList
            )
            adapter = adapterPlayList
        }


    }

    private fun fetchFolderUsingViewModel() {

        viewModel.fetchFolders(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersStateFLow.collect {
                    when (it) {
                        is ModelResult.Success -> {
                            fragmentListsBinding.progressBar.visibility = View.GONE
                            folderList.clear()
                            folderList.addAll(it.data!!)
                            val size = folderList.size
                            fragmentListsBinding.ListsFolderListSize.text =
                                if (size <= 1) "${size} folder " else "${size} folders "
                        }

                        is ModelResult.Error -> {
                            showToast(
                                requireContext(),
                                it.message.toString()
                            )
                        }

                        is ModelResult.Loading -> {
                        }

                        else -> {}
                    }
                }
            }
        }

    }


    private fun listeners() {

        fragmentListsBinding.LayoutFolderList.setOnClickListener {
            val intent = Intent(activity, FolderActivity::class.java)
            startActivity(intent)
        }

        fragmentListsBinding.LayoutMyFavourites.setOnClickListener {
            val intent = Intent(context, FavouriteActivity::class.java)
            startActivity(intent)
        }

        fragmentListsBinding.AddNewPlayListListFragment.setOnClickListener {
            newPlayList()
        }

    }


    private fun newPlayList() {
        val newPlayListSheetDialog =
            BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        val newPlayListSheetView =
            layoutInflater.inflate(R.layout.popup_dialog_newplaylist, null)
        newPlayListSheetDialog.setContentView(newPlayListSheetView)
        newPlayListSheetDialog.setCanceledOnTouchOutside(true)

        val newPlayListEditText =
            newPlayListSheetView.findViewById<EditText>(R.id.newPlayListEditTextDialog)
        val newPlayListOKText =
            newPlayListSheetView.findViewById<TextView>(R.id.newPlayListOKTextDialog)
        val newPlayListCancelText =
            newPlayListSheetView.findViewById<TextView>(R.id.newPlayListCancelTextDialog)
        newPlayListEditText.selectAll()
        newPlayListEditText.requestFocus()
        newPlayListSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        newPlayListOKText.setOnClickListener {
            val playlistName = newPlayListEditText.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                addPlayList(playlistName)
            } else {
                showToast(requireContext(), "Error in creating Playlist")
            }
            newPlayListSheetDialog.dismiss()
        }

        newPlayListCancelText.setOnClickListener {
            newPlayListSheetDialog.dismiss()
        }

        newPlayListSheetDialog.show()

    }

    private fun addPlayList(name: String) {
        var playListExist = false
//        for (i in musicPlayList.ref) {
//            if (name.equals(i.name, ignoreCase = true)) {
//                playListExist = true
//                break
//            }
//        }
        if (playListExist) {
            showToast(requireContext(), "PlayList Exist!!")
        } else {
//            val tempPlayList = PlayList()
//            tempPlayList.name = name
//            tempPlayList.playList = ArrayList()
//
//            musicPlayList.ref.add(tempPlayList)
//            playListAdapter.refreshPlayList()

        }

    }


}