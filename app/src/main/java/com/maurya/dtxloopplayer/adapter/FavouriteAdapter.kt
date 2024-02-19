package com.maurya.dtxloopplayer.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.databinding.MusicItemBinding
import com.maurya.dtxloopplayer.utils.formatDuration

class FavouriteAdapter(
    private val context: Context,
    private var musicList: ArrayList<MusicData>,
    private val playNext: Boolean = false

) :
    RecyclerView.Adapter<FavouriteAdapter.MusicHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
        return MusicHolder(MusicItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {


        holder.MusicName.text = musicList[position].title
        holder.MusicName.isSelected = true
        holder.MusicArist.text = musicList[position].artist
        holder.MusicArist.isSelected = true
        holder.MusicDuration.text = formatDuration(musicList[position].duration)

        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(holder.MusicArt)


        if (PlayerActivity.isInitialized) {
            val currentSongId =
                PlayerActivity.musicListPlayerActivity.getOrNull(PlayerActivity.musicPosition)?.id
            val musicItem = musicList[position].id

            if (currentSongId != null && currentSongId == musicItem) {
                holder.MusicName.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.MusicArist.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.MusicDuration.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.MusicName.setTextAppearance(R.style.TextViewStyle)
                    holder.MusicArist.setTextAppearance(R.style.TextViewStyle)
                    holder.MusicDuration.setTextAppearance(R.style.TextViewStyle)
                } else {
                    holder.MusicName.setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.MusicArist.setTextColor(ContextCompat.getColor(context, R.color.white))
                    holder.MusicDuration.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                }
            }
        } else {
            // Handle the case when PlayerActivity is not initialized (e.g., show an error message)
        }


        if (playNext) {
            holder.root.setOnClickListener {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("index", position)
                intent.putExtra("class", "PlayNext")
                ContextCompat.startActivity(context, intent, null)
            }
            holder.root.setOnLongClickListener {
//                val intent = Intent(context, PlayerActivity::class.java)
//                intent.putExtra("index", position)
//                intent.putExtra("class", "PlayNext")
//                ContextCompat.startActivity(context, intent, null)

                return@setOnLongClickListener true
            }
        } else {
            holder.root.setOnClickListener {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("index", position)
                intent.putExtra("class", "FavouriteAdapter")
                ContextCompat.startActivity(context, intent, null)
            }
        }

//
//        val itemCount = musicList.size
//        val itemCountText = if (itemCount == 1) {
//            "$itemCount song"
//        } else {
//            "$itemCount songs"
//        }
//        ListsFragment.fragmentListsBinding.ListsMyFavouritesSize.text = itemCountText


    }


    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateFavourites(newList: ArrayList<MusicData>) {
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }


    class MusicHolder(binding: MusicItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val MusicName = binding.MusicName
        val MusicArist = binding.MusicArtist
        val MusicDuration = binding.MusicDuration
        val MusicArt = binding.MusicArt
        val root = binding.root

    }


}