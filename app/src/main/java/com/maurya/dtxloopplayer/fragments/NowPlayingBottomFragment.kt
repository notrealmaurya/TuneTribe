package com.maurya.dtxloopplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.databinding.FragmentNowPlayingBottomBinding
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.setSongPosition


class NowPlayingBottomFragment : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var fragmentNowPlayingBottomBinding: FragmentNowPlayingBottomBinding
        var isInitialized = false

        lateinit var musicAdapter: MusicAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNowPlayingBottomBinding =
            FragmentNowPlayingBottomBinding.inflate(inflater, container, false)
        val view = fragmentNowPlayingBottomBinding.root

        isInitialized = true

        fragmentNowPlayingBottomBinding.root.visibility = View.INVISIBLE

        fragmentNowPlayingBottomBinding.songArtistMiniPlayer.isSelected = true
        fragmentNowPlayingBottomBinding.songNameMiniPlayer.isSelected = true


        listeners()

        return view
    }

    private fun listeners() {

        fragmentNowPlayingBottomBinding.root.setOnClickListener {
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                intent.putExtra("index", PlayerActivity.musicPosition)
                intent.putExtra("class", "NowPlaying")
                ContextCompat.startActivity(requireContext(), intent, null)
        }

        fragmentNowPlayingBottomBinding.queueNowPlayingFragment.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
            val bottomSheetView = layoutInflater.inflate(R.layout.dialog_queue, null)
            val recyclerView =
                bottomSheetView.findViewById<RecyclerView>(R.id.recyclerViewQueueActivity)
            val totalSongsTextView =
                bottomSheetView.findViewById<TextView>(R.id.totalSongsQueueActivity)

            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(13)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            musicAdapter =
                MusicAdapter(
                    requireContext(),
                    PlayerActivity.musicListPlayerActivity,
                    queueActivity = true
                )
            recyclerView.adapter = musicAdapter

            recyclerView.smoothScrollToPosition(PlayerActivity.musicPosition)

            val musicListSize = PlayerActivity.musicListPlayerActivity.size
            val songText = if (musicListSize == 1) "song" else "songs"
            totalSongsTextView.text = "Track Queue ($musicListSize $songText)"

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.setCanceledOnTouchOutside(true)

            // Set a fixed height for the Bottom Sheet Dialog (e.g., 400dp)
            val layoutParams = bottomSheetView.layoutParams
            layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.fixed_bottom_sheet_height) // Create a dimension resource for the fixed height
            bottomSheetView.layoutParams = layoutParams

            // Set BottomSheetBehavior to fixed height and disable dragging to expand
            val behavior = BottomSheetBehavior.from(bottomSheetView.parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false

            bottomSheetDialog.show()

            musicAdapter.notifyDataSetChanged()
        }


        fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }

        fragmentNowPlayingBottomBinding.NextMiniPlayer.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)

            PlayerActivity.binding.songNAME.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].title
            PlayerActivity.binding.songARTIST.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artist

            fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].title
            fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artist

            PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause, "Play")
            playMusic()
            notifyAdapterSongTextPosition()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            fragmentNowPlayingBottomBinding.root.visibility = View.VISIBLE

            Glide.with(this)
                .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)


            fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].title
            fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].artist
            if (PlayerActivity.isPlaying) fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
                R.drawable.icon_pause
            )
            else fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_play)

        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
            R.drawable.icon_pause
        )
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause, "Pause")
        PlayerActivity.binding.nextSongPlayerActivity.setImageResource(R.drawable.icon_pause)

    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
            R.drawable.icon_play
        )
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_play, "Play")
        PlayerActivity.binding.nextSongPlayerActivity.setImageResource(R.drawable.icon_play)
    }

}