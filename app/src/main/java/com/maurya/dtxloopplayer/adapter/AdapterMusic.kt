package com.maurya.dtxloopplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.maurya.dtxloopplayer.utils.sendIntent

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

        val currentItem = musicList[position]

        with(holder) {
            musicName.isSelected = true
            musicArist.isSelected = true
            musicName.text = currentItem.musicName
            musicArist.text = currentItem.albumArtist
            musicDuration.text = DateUtils.formatElapsedTime(currentItem.durationText / 1000)

            Glide.with(context)
                .asBitmap()
                .load(currentItem.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.icon_music)
                .into(musicArt)

        }

        when {
            playListActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(context, reference = "PlayListActivity", position = position)
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
                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent(
                                context,
                                reference = "NowPlaying",
                                position = PlayerActivity.musicPosition
                            )

                        else -> {
                            sendIntent(context, reference = "SongsFragment", position = position)
                        }


                    }

                }
            }

        }
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
        val musicName = binding.musicNameMusicItem
        val musicArist = binding.artistNameMusicItem
        val musicDuration = binding.musicDurationMusicItem
        val musicArt = binding.musicImageMusicItem
        val root = binding.root
        val chekbox = binding.checkBoxMusicItem


    }


}