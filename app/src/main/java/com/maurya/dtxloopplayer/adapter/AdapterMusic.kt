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
import com.maurya.dtxloopplayer.activities.SelectionActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ItemMusicBinding
import com.maurya.dtxloopplayer.fragments.ListsFragment
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.showToast

class AdapterMusic(
    private val context: Context,
    private var musicList: ArrayList<MusicDataClass> = arrayListOf(),
    private val listener: MediaControlInterface? = null,
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
                    musicArt.visibility = View.GONE
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
                    listener?.onSongSelected(musicList, position)
                }
            }

            selectionActivity -> {
                with(holder) {
                    musicName.isSelected = false
                    musicArist.isSelected = false
                    checkbox.visibility = View.VISIBLE
                    checkbox.isClickable = false
                    checkbox.isChecked = isSongAdded(musicList[position])
                    SelectionActivity.selectionCount = PlayListActivity.currentPlayListMusicList.size

                    root.setOnClickListener {
                        val musicData = musicList[position]
                        val isAdded = addOrRemoveSong(musicData)
                        checkbox.isChecked = isAdded
                    }
                }

            }

            queueActivity -> {
                holder.root.setOnClickListener {
                    listener?.onSongSelected(musicList, position)
                }
            }

            folderSongsActivity -> {
                holder.root.setOnClickListener {
                    listener?.onSongSelected(musicList, position)
                }
            }

            searchActivity -> {
                holder.root.setOnClickListener {
                    if (SearchActivity.search) {
                        listener?.onSongSelected(musicList, position)
                    }
                }
            }

            favouriteActivity -> {
                holder.root.setOnClickListener {
                    listener?.onSongSelected(musicList, position)
                }

            }


            else -> {
                holder.root.setOnClickListener {
                    when {
                        musicList[position].id == MainActivity.nowPlayingId ->
                            showToast(context, "This song is currently Playing")

                        else -> {
                            listener?.onSongSelected(musicList, position)
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
        SelectionActivity.selectionCount = PlayListActivity.currentPlayListMusicList.size

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
        val checkbox = binding.checkBoxMusicItem


    }


}