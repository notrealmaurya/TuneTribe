package com.maurya.dtxloopplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.FolderActivity
import com.maurya.dtxloopplayer.adapter.AdapterFolder
import com.maurya.dtxloopplayer.adapter.AdapterPlayList
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.database.PlayListDataClass
import com.maurya.dtxloopplayer.databinding.FragmentListsBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogNewplaylistBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogPlayeractivityMenuBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class ListsFragment : Fragment() {

    private lateinit var folderAdapter: AdapterFolder


    private val viewModel: ViewModelObserver by viewModels()

    private lateinit var fragmentListsBinding: FragmentListsBinding

    private lateinit var adapterPlayList: AdapterPlayList

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

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
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fetchFolderUsingViewModel()
        fetchPlayListUsingViewModel()

        val playListPreference = sharedPreferenceHelper.getPlayList()
        playList.addAll(playListPreference)

        listeners()
    }

    private fun fetchPlayListUsingViewModel() {

        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterPlayList = AdapterPlayList(
                requireContext(),
                playList, sharedPreferenceHelper
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

        fragmentListsBinding.ListsMyFavouritesSize.text =
            updateTextViewWithItemCount(sharedPreferenceHelper.getPlayListSongCount("myFavouriteYouNoty572notyCount"))

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
            layoutInflater.inflate(
                R.layout.popup_dialog_newplaylist,
                fragmentListsBinding.root,
                false
            )
        val bindingNewPlayList = PopupDialogNewplaylistBinding.bind(newPlayListSheetView)
        newPlayListSheetDialog.setContentView(newPlayListSheetView)
        newPlayListSheetDialog.setCanceledOnTouchOutside(true)

        bindingNewPlayList.newPlayListEditTextDialog.selectAll()
        bindingNewPlayList.newPlayListEditTextDialog.requestFocus()
        newPlayListSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        bindingNewPlayList.newPlayListOKTextDialog.setOnClickListener {
            val playlistName = bindingNewPlayList.newPlayListEditTextDialog.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                addPlayList(playlistName)
            } else {
                showToast(requireContext(), "Error in creating Playlist")
            }
            newPlayListSheetDialog.dismiss()
        }

        bindingNewPlayList.newPlayListCancelTextDialog.setOnClickListener {
            newPlayListSheetDialog.dismiss()
        }
        newPlayListSheetDialog.show()

    }

    private fun addPlayList(name: String) {
        var playListExist = false
        for (i in playList) {
            if (name.equals(i.playListName, ignoreCase = true)) {
                playListExist = true
                break
            }
        }
        if (playListExist) {
            showToast(requireContext(), "PlayList Exist!!")
        } else {
            val playlistId = System.currentTimeMillis().toString()
            val savePlayList = PlayListDataClass(playlistId, name, System.currentTimeMillis(), 0)
            playList.add(savePlayList)
            sharedPreferenceHelper.savePlayList(playList)
            adapterPlayList.notifyDataSetChanged()

        }
    }


    override fun onResume() {
        super.onResume()
        adapterPlayList.notifyDataSetChanged()
        fragmentListsBinding.ListsMyFavouritesSize.text =
            updateTextViewWithItemCount(sharedPreferenceHelper.getPlayListSongCount("myFavouriteYouNoty572notyCount"))
    }


}