package com.maurya.dtxloopplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.activities.FolderTracksActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.dataEntity.FolderData
import com.maurya.dtxloopplayer.dataEntity.countMusicFilesInFolder
import com.maurya.dtxloopplayer.databinding.PlaylistViewBinding

class FolderAdapter(
    private val context: Context,
    private var folderList: List<FolderData>
) :
    RecyclerView.Adapter<FolderAdapter.FolderHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        return FolderHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: FolderHolder, position: Int) {

        val folderPath = folderList[position].folderPath
        val musicFileCount = countMusicFilesInFolder(folderPath)


        holder.folderName.text = folderList[position].folderName +" ($musicFileCount)"
        holder.folderName.isSelected = true

        val modifiedPath = modifyFolderPath(folderList[position].folderPath)

        holder.folderPath.text = "Path : " + modifiedPath
        holder.folderPath.isSelected = true
        holder.folderImage.setImageResource(R.drawable.icon_folder_subitems)

        // In FolderAdapter, when a folder item is clicked
        holder.itemView.setOnClickListener {
            val folderPath = folderList[position].folderPath // Get the folder path
            val intent = Intent(context, FolderTracksActivity::class.java)
            intent.putExtra("folderPath", folderPath) // Put the folder path as an extra
            context.startActivity(intent)
        }


    }


    private fun modifyFolderPath(originalPath: String): String {
        return "Internal Storage${originalPath.substringAfter("/storage/emulated/0")}"
    }

    override fun getItemCount(): Int {
        return folderList.size
    }


    class FolderHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.ListsMyPlayListsName
        val folderPath = binding.ListsMyPlayListsSize
        val folderImage = binding.img
        val root = binding.root
    }





}