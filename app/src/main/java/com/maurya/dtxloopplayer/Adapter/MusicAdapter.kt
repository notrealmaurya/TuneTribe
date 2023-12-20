package com.maurya.dtxloopplayer.Adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.maurya.dtxloopplayer.Activities.PlayListActivity
import com.maurya.dtxloopplayer.MusicData
import com.maurya.dtxloopplayer.Activities.PlayerActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.Activities.SearchActivity
import com.maurya.dtxloopplayer.Activities.SelectionActivity
import com.maurya.dtxloopplayer.Fragments.ListsFragment
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.databinding.MusicItemBinding
import com.maurya.dtxloopplayer.formatDuration

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<MusicData>,
    private val playListActivity: Boolean = false,
    private val selectionActivity: Boolean = false,
    private val folderSongsActivity: Boolean = false,
    private val queueActivity: Boolean = false,
    private val searchActivity: Boolean = false
) :
    RecyclerView.Adapter<MusicAdapter.MusicHolder>() {

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



        when {
            playListActivity -> {
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlayListActivity", position = position)
                }
            }

            selectionActivity -> {
                holder.chekbox.visibility = View.VISIBLE
                holder.chekbox.isClickable = false
                holder.chekbox.isChecked = isSongAdded(musicList[position])

                holder.root.setOnClickListener {
                    val musicData = musicList[position]
                    val added = addSong(musicData)
                    holder.chekbox.isChecked = added
                    updateSelectedCountSelectionActivity() // Call the function to update the selected count
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

    fun updateMusicList(searchList: ArrayList<MusicData>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun updateSelectedCountSelectionActivity() {
        var checkedCount = 0
        for (musicData in musicList) {
            if (isSongAdded(musicData)) {
                checkedCount++
            }
        }
        SelectionActivity.binding.selectedSongSelectionActivity.text =
            "Selected: $checkedCount songs"
    }

    private fun addSong(musicData: MusicData): Boolean {
        ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList.forEachIndexed { index, music ->
            if (musicData.id == music.id) {
                ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList.removeAt(
                    index
                )
                return false
            }
        }
        ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList.add(
            musicData
        )
        return true
    }


    private fun isSongAdded(musicData: MusicData): Boolean {
        val playList = ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition]
        return playList.playList.any { it.id == musicData.id }
    }


    fun refreshPlayList() {
        musicList = ArrayList()
        musicList =
            ListsFragment.musicPlayList.ref[PlayListActivity.currentPlayListPosition].playList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    class MusicHolder(binding: MusicItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val MusicName = binding.MusicName
        val MusicArist = binding.MusicArtist
        val MusicDuration = binding.MusicDuration
        val MusicArt = binding.MusicArt
        val root = binding.root
        val chekbox = binding.checkboxMusicItem


    }


}