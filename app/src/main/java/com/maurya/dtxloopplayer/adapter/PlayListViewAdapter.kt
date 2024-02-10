package com.maurya.dtxloopplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.dataEntity.PlayList
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.databinding.PlaylistViewBinding

class PlayListViewAdapter(
    private val context: Context,
    private var playListList: ArrayList<PlayList>,
    private val selectionActivity: Boolean = false

) :
    RecyclerView.Adapter<PlayListViewAdapter.PlayListHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListHolder {
        return PlayListHolder(
            PlaylistViewBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlayListHolder, position: Int) {

        holder.PlayListSize.isSelected = true
        holder.PlayListName.isSelected = true

        holder.PlayListName.text = playListList[position].name
       // holder.PlayListSize.text = "${adapter.itemCount} songs"




        holder.root.setOnClickListener {
            val intent = Intent(context, PlayListActivity::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }

        holder.root.setOnLongClickListener {
            showBottomSheetDialog(playListList[position], position)
            true
        }

    }




    override fun getItemCount(): Int {
        return playListList.size
    }


    private fun showBottomSheetDialog(playlist: PlayList, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.playlist_listfragment_popup_onlongclick, null)

        val renameOption =
            sheetView.findViewById<LinearLayout>(R.id.PlayListListFragmentPopUpRename)
        val deleteOption =
            sheetView.findViewById<LinearLayout>(R.id.PlayListListFragmentPopUpDelete)

        //rename option
        renameOption.setOnClickListener {
            val renameSheetDialog =
                BottomSheetDialog(context, R.style.ThemeOverlay_App_BottomSheetDialog)
            val renameSheetView =
                LayoutInflater.from(context).inflate(R.layout.bottomsheet_rename, null)

            renameSheetDialog.setContentView(renameSheetView)
            renameSheetDialog.setCanceledOnTouchOutside(true)

            val renameEditText = renameSheetView.findViewById<EditText>(R.id.rename_EditText)
            val rename_CancelText = renameSheetView.findViewById<TextView>(R.id.rename_CancelText)
            val rename_OKText = renameSheetView.findViewById<TextView>(R.id.rename_OKText)

            if (renameEditText != null) {
                renameEditText.requestFocus() // Set focus on the EditText
                renameEditText.setText(ListsFragment.musicPlayList.ref.get(position).name)
                renameEditText.setSelection(
                    0,
                    ListsFragment.musicPlayList.ref.get(position).name.length
                )
                renameSheetDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }

            rename_OKText.setOnClickListener {
                val newName = renameEditText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    ListsFragment.musicPlayList.ref.get(position).name = newName
                    refreshPlayList()
                    renameSheetDialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }

            rename_CancelText.setOnClickListener {
                renameSheetDialog.dismiss() // Dismiss the renaming dialog
            }
            renameSheetDialog.show()

            bottomSheetDialog.dismiss()
        }

        //delete option
        deleteOption.setOnClickListener {
            bottomSheetDialog.dismiss()

            val alertDialog = MaterialAlertDialogBuilder(context)
            alertDialog.setTitle(playListList[position].name)
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Delete") { dialog, _ ->
                    ListsFragment.musicPlayList.ref.removeAt(position)
                    refreshPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }


    fun refreshPlayList() {
        playListList = ArrayList()
        playListList.addAll(ListsFragment.musicPlayList.ref)
        notifyDataSetChanged()
    }


    class PlayListHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val PlayListName = binding.ListsMyPlayListsName
        val PlayListSize = binding.ListsMyPlayListsSize
        val root = binding.root

    }


}