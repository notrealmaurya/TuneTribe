package com.maurya.dtxloopplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.activities.SelectionActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ItemMusicBinding
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.utils.formatDuration

class AdapterMusic(
    private val context: Context,
    private var musicList: ArrayList<MusicDataClass> = arrayListOf(),
    private val playListActivity: Boolean = false,
    private val selectionActivity: Boolean = false,
    private val folderSongsActivity: Boolean = false,
    private val queueActivity: Boolean = false,
    private val searchActivity: Boolean = false,
    private val favouriteActivity: Boolean = false
) :
    RecyclerView.Adapter<AdapterMusic.MusicHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
        return MusicHolder(ItemMusicBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {

        holder.MusicName.text = musicList[position].musicName
        holder.MusicName.isSelected = true
        holder.MusicArist.text = musicList[position].albumArtist
        holder.MusicArist.isSelected = true
        holder.MusicDuration.text = formatDuration(musicList[position].durationText)
        Glide.with(context)
            .load(musicList[position].image)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(holder.MusicArt)


        when {
            playListActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlayListActivity", position = position)
                }
            }

            selectionActivity -> {
                holder.chekbox.visibility = View.VISIBLE
                holder.chekbox.isClickable = false
//                holder.chekbox.isChecked = isSongAdded(musicList[position])

                holder.root.setOnClickListener {
                    val musicData = musicList[position]
//                    val added = addSong(musicData)
//                    holder.chekbox.isChecked = added
                }
            }

            queueActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "queueActivity", position = position)
                }
            }

            folderSongsActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "folderSongsActivity", position = position)
                }
            }

            searchActivity -> {
                holder.root.setOnClickListener {
                    if (SearchActivity.search) {
                        sendIntent(
                            ref = "MusicAdapterSearch",
                            position = position
                        )
                    }
                }
            }

            favouriteActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "FavouriteAdapter", position = position)
                }

            }


            else -> {
                holder.root.setOnClickListener {
                    when {
                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent(ref = "NowPlaying", position = PlayerActivity.musicPosition)

                        else -> {
                            sendIntent(ref = "SongsFragment", position = position)
                        }


                    }

                }
            }

        }
    }

    private fun sendIntent(ref: String, position: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("class", ref)
        intent.putExtra("index", position)
        ContextCompat.startActivity(context, intent, null)
    }


    fun updateFavourites(newList: ArrayList<MusicDataClass>) {
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }



    fun refreshPlayList() {
        musicList = ArrayList()
//        musicList =
//            ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchList(searchList: ArrayList<MusicDataClass>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    class MusicHolder(binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        val MusicName = binding.MusicName
        val MusicArist = binding.MusicArtist
        val MusicDuration = binding.MusicDuration
        val MusicArt = binding.MusicArt
        val root = binding.root
        val chekbox = binding.checkboxMusicItem


    }


}