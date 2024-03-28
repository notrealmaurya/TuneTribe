package com.maurya.dtxloopplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ItemMusicBinding
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.showToast

class AdapterMusic(
    private val context: Context,
    private var musicList: ArrayList<MusicDataClass> = arrayListOf(),
    private var sharedPreferenceHelper: SharedPreferenceHelper? = null,
    private var uuidCurrentPlayList: String = "",
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

        val currentItem = musicList[position]

        with(holder) {
            musicName.isSelected = true
            musicArist.isSelected = true
            musicName.text = currentItem.musicName
            musicArist.text = currentItem.albumArtist
            musicDuration.text = DateUtils.formatElapsedTime(currentItem.durationText / 1000)

            when {
                selectionActivity -> {
                    Glide.with(context)
                        .asBitmap()
                        .load(R.drawable.icon_music)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .error(R.drawable.icon_music)
                        .into(musicArt)
                }

                else -> {
                    Glide.with(context)
                        .asBitmap()
                        .load(currentItem.image)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .error(R.drawable.icon_music)
                        .into(musicArt)
                }
            }
        }

        when {
            playListActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(context, reference = "PlayListActivity", position = position)
                }
            }

            selectionActivity -> {
                with(holder) {
                    chekbox.visibility = View.VISIBLE
                    chekbox.isClickable = false
                    chekbox.isChecked = isSongAdded(musicList[position])

                    root.setOnClickListener {
                        val musicData = musicList[position]
                        val isAdded = addOrRemoveSong(musicData)
                        chekbox.isChecked = isAdded
                    }
                }

            }

            queueActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(context, reference = "queueActivity", position = position)
                }
            }

            folderSongsActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(context, reference = "folderSongsActivity", position = position)
                }
            }

            searchActivity -> {
                holder.root.setOnClickListener {
                    if (SearchActivity.search) {
                        sendIntent(
                            context, reference = "MusicAdapterSearch",
                            position = position
                        )
                    }
                }
            }

            favouriteActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(context, reference = "FavouriteAdapter", position = position)
                }

            }


            else -> {
                holder.root.setOnClickListener {
                    when {
                        musicList[position].id == MainActivity.nowPlayingId ->
                            showToast(context, "This song is currently Playing")

                        else -> {
                            sendIntent(context, reference = "SongsFragment", position = position)
                        }

                    }

                }
            }

        }
    }


    //for playlist selection activity
    private fun isSongAdded(musicData: MusicDataClass): Boolean {
        return PlayListActivity.currentPlayListMusicList.contains(musicData)
    }


    //for playlist selection activity
    private fun addOrRemoveSong(musicData: MusicDataClass): Boolean {
        val isAdded = isSongAdded(musicData)
        if (isAdded) {
            PlayListActivity.currentPlayListMusicList.remove(musicData)
        } else {
            PlayListActivity.currentPlayListMusicList.add(musicData)
        }

        // Save playlist and its count
        sharedPreferenceHelper?.savePlayListSong(
            PlayListActivity.currentPlayListMusicList,
            uuidCurrentPlayList
        )
        sharedPreferenceHelper?.savePlayListSongCount(
            PlayListActivity.currentPlayListMusicList.size,
            uuidCurrentPlayList
        )

        return !isAdded
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
        val musicName = binding.musicNameMusicItem
        val musicArist = binding.artistNameMusicItem
        val musicDuration = binding.musicDurationMusicItem
        val musicArt = binding.musicImageMusicItem
        val root = binding.root
        val chekbox = binding.checkBoxMusicItem


    }


}