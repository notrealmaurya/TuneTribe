package com.maurya.dtxloopplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.activities.SelectionActivity
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ItemMusicBinding
import com.maurya.dtxloopplayer.fragments.PlayListFragment
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
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

    private var previousPlayingId: String = ""
    private var currentPlayingId: String = ""

    fun updatePlaybackState(newPlayingId: String) {
        previousPlayingId = currentPlayingId
        currentPlayingId = newPlayingId
        val previousPlayingPosition = getPositionById(previousPlayingId)
        val currentPlayingPosition = getPositionById(currentPlayingId)
        notifyItemChanged(previousPlayingPosition)
//        notifyItemChanged(currentPlayingPosition)
        notifyItemChanged(MainActivity.musicPosition)


        Log.d("prevItemClass", previousPlayingId)
        Log.d("prevItemClass", previousPlayingPosition.toString())
        Log.d("curItemClass", currentPlayingId)
        Log.d("curItemClass", currentPlayingPosition.toString())
    }

    private fun getPositionById(id: String): Int {
        for ((index, item) in musicList.withIndex()) {
            if (item.id == id) {
                return index
            }
        }
        return -1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
        return MusicHolder(ItemMusicBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {

        val currentItem = musicList[position]

        with(holder) {
            musicName.isSelected = true
            musicArtist.isSelected = true
            musicName.text = currentItem.musicName
            musicArtist.text = currentItem.albumArtist
            musicDuration.text = DateUtils.formatElapsedTime(currentItem.durationText / 1000)

            val redColor = ContextCompat.getColor(context, R.color.red)
            val defaultColor = ContextCompat.getColor(context, R.color.ImageViewAndTextViewColour)

            if (currentItem.id == currentPlayingId) {
                holder.musicName.setTextColor(redColor)
                holder.musicArtist.setTextColor(redColor)
                holder.musicDuration.visibility = View.GONE
                holder.lottieView.visibility = View.VISIBLE
            } else {
                holder.musicName.setTextColor(defaultColor)
                holder.musicArtist.setTextColor(defaultColor)
                holder.musicDuration.visibility = View.VISIBLE
                holder.lottieView.visibility = View.GONE
            }

            when {
                selectionActivity -> {
                    musicArt.visibility = View.GONE
                }

                queueActivity -> {
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
                    musicArtist.isSelected = false
                    checkbox.visibility = View.VISIBLE
                    checkbox.isClickable = false
                    checkbox.isChecked = isSongAdded(musicList[position])
                    SelectionActivity.selectionCount =
                        PlayListFragment.currentPlayListMusicList.size

                    root.setOnClickListener {
                        val musicData = musicList[position]
                        val isAdded = addOrRemoveSong(musicData)
                        checkbox.isChecked = isAdded
                    }
                }

            }

            queueActivity -> {
                holder.musicArtist.isSelected = false
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
        return PlayListFragment.currentPlayListMusicList.contains(musicData)
    }


    //for playlist selection activity
    private fun addOrRemoveSong(musicData: MusicDataClass): Boolean {
        val isAdded = isSongAdded(musicData)
        if (isAdded) {
            PlayListFragment.currentPlayListMusicList.remove(musicData)
        } else {
            PlayListFragment.currentPlayListMusicList.add(musicData)
        }
        SelectionActivity.selectionCount = PlayListFragment.currentPlayListMusicList.size

        // Save playlist and its count
        sharedPreferenceHelper?.savePlayListSong(
            PlayListFragment.currentPlayListMusicList,
            uuidCurrentPlayList
        )
        sharedPreferenceHelper?.savePlayListSongCount(
            PlayListFragment.currentPlayListMusicList.size,
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
        val musicArtist = binding.artistNameMusicItem
        val musicDuration = binding.musicDurationMusicItem
        val musicArt = binding.musicImageMusicItem
        val root = binding.root
        val checkbox = binding.checkBoxMusicItem
        val lottieView = binding.lotteView


    }


}