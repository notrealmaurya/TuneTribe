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
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.database.PlayListDataClass
import com.maurya.dtxloopplayer.databinding.ItemPlaylistBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogPlayeractivityMenuBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogPlaylistEditBinding
import com.maurya.dtxloopplayer.databinding.PopupDialogRenameBinding
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.showToast
import java.util.UUID

class AdapterPlayList(
    private val context: Context,
    private var playListList: ArrayList<PlayListDataClass>,
    private var sharedPreferenceHelper: SharedPreferenceHelper
) :
    RecyclerView.Adapter<AdapterPlayList.PlayListHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListHolder {
        return PlayListHolder(
            ItemPlaylistBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlayListHolder, position: Int) {

        val currentItem = playListList[position]

        with(holder) {
            playListSize.isSelected = true
            playListName.isSelected = true
            playListName.text = currentItem.playListName
            // playListSize.text = "${adapter.itemCount} songs"


            root.setOnClickListener {
                sendIntent(context, position, "adapterPlayList")
            }

            root.setOnLongClickListener {
                showBottomSheetDialog(position)
                true
            }
        }

    }


    override fun getItemCount(): Int {
        return playListList.size
    }


    private fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView =
            LayoutInflater.from(context)
                .inflate(R.layout.popup_dialog_playlist_edit, null)
        val bindingPopUp = PopupDialogPlaylistEditBinding.bind(bottomSheetView)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        //rename option
        bindingPopUp.popUpRename.setOnClickListener {
            bottomSheetDialog.dismiss()
            val renameSheetDialog =
                BottomSheetDialog(context)
            val renameSheetView =
                LayoutInflater.from(context).inflate(R.layout.popup_dialog_rename, null)
            val bindingRenamePopUp = PopupDialogRenameBinding.bind(renameSheetView)
            renameSheetDialog.setContentView(renameSheetView)
            renameSheetDialog.setCanceledOnTouchOutside(true)
            renameSheetDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            bindingRenamePopUp.renameEditText.requestFocus()
            bindingRenamePopUp.renameEditText.setText(playListList[position].playListName)
            bindingRenamePopUp.renameEditText.selectAll()

            bindingRenamePopUp.renameOKText.setOnClickListener {
                val newName = bindingRenamePopUp.renameEditText.text.toString().trim()
                var playListExist = false
                for (i in playListList) {
                    if (newName.equals(i.playListName, ignoreCase = true)) {
                        playListExist = true
                        break
                    }
                }
                if (playListExist && newName.isNotEmpty()) {
                    showToast(context, "PlayList Exist!!")
                } else {
                    playListList[position].playListName = newName
                    playListList[position].dateModified = System.currentTimeMillis()
                    sharedPreferenceHelper.savePlayList(playListList)
                    notifyDataSetChanged()
                    renameSheetDialog.dismiss()
                }

            }
            bindingRenamePopUp.renameCancelText.setOnClickListener {
                renameSheetDialog.dismiss()
            }

            renameSheetDialog.show()
        }

        //delete option
        bindingPopUp.popUpDelete.setOnClickListener {
            bottomSheetDialog.dismiss()

            val alertDialog = MaterialAlertDialogBuilder(context)
            alertDialog.setTitle(playListList[position].playListName)
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Delete") { dialog, _ ->
                    playListList.removeAt(position)
                    sharedPreferenceHelper.savePlayList(playListList)
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }

        bottomSheetDialog.show()
    }


    class PlayListHolder(binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        val playListName = binding.ListsMyPlayListsName
        val playListSize = binding.ListsMyPlayListsSize
        val root = binding.root

    }


}