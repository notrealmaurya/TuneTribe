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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.FolderActivity
import com.maurya.dtxloopplayer.adapter.FolderAdapter
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.adapter.PlayListViewAdapter
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.database.MusicFolderScanner
import com.maurya.dtxloopplayer.database.MusicPlayList
import com.maurya.dtxloopplayer.database.PlayList
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.databinding.FragmentListsBinding
import com.maurya.dtxloopplayer.utils.updateTextViewWithFolderCount
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount


class ListsFragment : Fragment() {

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recyclerViewRecentlyPlayed: RecyclerView

    private lateinit var recyclerView: RecyclerView

    companion object {
        var musicPlayList: MusicPlayList = MusicPlayList()
        const val TAG = "ListFragment"

        @SuppressLint("StaticFieldLeak")
        lateinit var fragmentListsBinding: FragmentListsBinding

        @SuppressLint("StaticFieldLeak")
        lateinit var playListAdapter: PlayListViewAdapter

        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentListsBinding = FragmentListsBinding.inflate(inflater, container, false)
        val view = fragmentListsBinding.root


        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.setHasFixedSize(true)
        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.setItemViewCacheSize(13)
        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.layoutManager =
            LinearLayoutManager(context)
        playListAdapter = PlayListViewAdapter(requireContext(), playListList = musicPlayList.ref)
        fragmentListsBinding.recyclerViewListFragmentForMyPlayList.adapter = playListAdapter



        sharedPreferenceRetrievingData()
        updateText()

        listeners()

        return view
    }


    private fun sharedPreferenceRetrievingData() {

        //for retrieving favourites data using shared preferences
        FavouriteActivity.favouriteSongs = ArrayList()
        val typeToken = object : TypeToken<ArrayList<MusicData>>() {}.type
        val jsonString = requireActivity().getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            .getString("FavouriteSongs", null)
        if (jsonString != null) {
            val data: ArrayList<MusicData> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavouriteActivity.favouriteSongs.addAll(data)
        }

        //for retrieving PlayList data using shared preferences
        musicPlayList = MusicPlayList()
        val jsonStringPlaylist = requireActivity().getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            .getString("MusicPlaylist", null)
        if (jsonStringPlaylist != null) {
            val dataPlaylist: MusicPlayList =
                GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlayList::class.java)
            musicPlayList = dataPlaylist
        }


    }

    private fun sharedPreferenceStoringData() {
        // For storing favorite songs data using shared preferences
        val editorFav = requireActivity().getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringFav = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editorFav.putString("FavouriteSongs", jsonStringFav)
        //for playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(musicPlayList)
        editorFav.putString("MusicPlaylist", jsonStringPlaylist)
        playListAdapter.notifyDataSetChanged()

    }

    private fun updateText() {

        //Size of MyFavourite
        musicAdapter = MusicAdapter(
            requireContext(),
            FavouriteActivity.favouriteSongs,
            favouriteActivity = true
        )
        updateTextViewWithItemCount(musicAdapter, fragmentListsBinding.ListsMyFavouritesSize)

        //Size of FolderList
        val musicFolderScanner = MusicFolderScanner(requireActivity().contentResolver)
        val musicFolders = musicFolderScanner.getAllMusicFolders()
        folderAdapter = FolderAdapter(requireContext(), musicFolders)
        updateTextViewWithFolderCount(folderAdapter, fragmentListsBinding.ListsFolderListSize)

    }

    override fun onResume() {
        super.onResume()
        updateText()
    }


    override fun onPause() {
        super.onPause()
        sharedPreferenceStoringData()
        updateText()

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
            layoutInflater.inflate(R.layout.playlist_listfragment_newplaylist_dialog, null)
        newPlayListSheetDialog.setContentView(newPlayListSheetView)
        newPlayListSheetDialog.setCanceledOnTouchOutside(true)


        val newPlayListEditText =
            newPlayListSheetView.findViewById<EditText>(R.id.newPlayListEditTextDialog)
        val newPlayListOKText =
            newPlayListSheetView.findViewById<TextView>(R.id.newPlayListOKTextDialog)
        val newPlayListCancelText =
            newPlayListSheetView.findViewById<TextView>(R.id.newPlayListCancelTextDialog)
        newPlayListEditText.selectAll() // select all edit text
        newPlayListEditText.requestFocus() // open keyboard
        newPlayListSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) // Show the keyboard below card


        newPlayListOKText.setOnClickListener {
            val playlistName = newPlayListEditText.text.toString().trim()
            if (playlistName != null) {
                if (playlistName.isNotEmpty()) {
                    addPlayList(playlistName.toString())
                }
            } else {
                Toast.makeText(context, "Set playlist Name", Toast.LENGTH_SHORT).show()
            }
            newPlayListSheetDialog.dismiss()

        }

        newPlayListCancelText.setOnClickListener {
            // Handle Cancel button click
            newPlayListSheetDialog.dismiss()
        }

        newPlayListSheetDialog.show()

    }


    private fun addPlayList(name: String) {
        var playListExist = false
        for (i in musicPlayList.ref) {
            if (name.equals(i.name, ignoreCase = true)) {
                playListExist = true
                break
            }
        }
        if (playListExist) {
            Toast.makeText(context, "PlayList Exist!!", Toast.LENGTH_SHORT).show()
        } else {
            val tempPlayList = PlayList()
            tempPlayList.name = name
            tempPlayList.playList = ArrayList()

            musicPlayList.ref.add(tempPlayList)
            playListAdapter.refreshPlayList()

        }

    }


}