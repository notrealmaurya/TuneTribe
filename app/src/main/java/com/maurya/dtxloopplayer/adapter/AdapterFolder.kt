package com.maurya.dtxloopplayer.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.databinding.ItemPlaylistBinding
import com.maurya.dtxloopplayer.fragments.FavouriteFragment
import com.maurya.dtxloopplayer.fragments.FolderTracksFragment
import com.maurya.dtxloopplayer.utils.countMusicFilesInFolder

class AdapterFolder(
    private val context: Context,
    private var folderList: ArrayList<FolderDataClass>,
    private val fragmentManager: FragmentManager
) :
    RecyclerView.Adapter<AdapterFolder.FolderHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        return FolderHolder(
            ItemPlaylistBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FolderHolder, position: Int) {

        val folderPath = folderList[position].folderPath
        val musicFileCount = countMusicFilesInFolder(context, folderPath)

        holder.folderName.text = folderList[position].folderName + " ($musicFileCount)"

        holder.folderName.isSelected = true

        val modifiedPath = modifyFolderPath(folderList[position].folderPath)

        holder.folderPath.text = "Path : " + modifiedPath
        holder.folderPath.isSelected = true
        holder.folderImage.setImageResource(R.drawable.icon_folder_subitems)

        // In FolderAdapter, when a folder item is clicked
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("folderPath", folderPath)

            val receivingFragment = FolderTracksFragment()
            receivingFragment.arguments = bundle

            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.containerMainActivity, receivingFragment)
            transaction.commit()

        }

    }


    private fun modifyFolderPath(originalPath: String): String {
        return "Internal Storage${originalPath.substringAfter("/storage/emulated/0")}"
    }

    override fun getItemCount(): Int {
        return folderList.size
    }


    class FolderHolder(binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.namePlayListItem
        val folderPath = binding.countPlayListItem
        val folderImage = binding.img
        val root = binding.root
    }


}