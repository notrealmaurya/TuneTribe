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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityPlayerBinding
import com.maurya.dtxloopplayer.databinding.FragmentNowPlayingBottomBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.createMediaPlayer
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.pauseMusic
import com.maurya.dtxloopplayer.utils.playMusic
import com.maurya.dtxloopplayer.utils.prevNextSong
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.setSongPosition
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject


@AndroidEntryPoint
class NowPlayingBottomFragment : Fragment() {

    private lateinit var musicAdapter: AdapterMusic
    private lateinit var fragmentNowPlayingBottomBinding: FragmentNowPlayingBottomBinding

    companion object {

        lateinit var viewModel: ViewModelObserver

        private var bindingRef: WeakReference<FragmentNowPlayingBottomBinding>? = null

        fun getNowPlayingFragmentBinding(): FragmentNowPlayingBottomBinding? {
            return bindingRef?.get()
        }
    }


    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNowPlayingBottomBinding =
            FragmentNowPlayingBottomBinding.inflate(inflater, container, false)
        val view = fragmentNowPlayingBottomBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindingRef = WeakReference(fragmentNowPlayingBottomBinding)

//        fragmentNowPlayingBottomBinding.root.visibility = View.INVISIBLE

        viewModel = ViewModelProvider(this)[ViewModelObserver::class.java]

        viewModel.songInfo.observe(viewLifecycleOwner) { musicData ->
            fragmentNowPlayingBottomBinding.songNameMiniPlayer.text = musicData.musicName
            fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text = musicData.albumArtist
            Glide.with(this)
                .asBitmap()
                .load(musicData.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.icon_music)
                .into(fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)
        }

        listeners()
    }

    private fun listeners() {

        fragmentNowPlayingBottomBinding.root.setOnClickListener {
            sendIntent(
                requireContext(),
                position = PlayerActivity.musicPosition,
                reference = "NowPlaying"
            )
        }

        fragmentNowPlayingBottomBinding.queueNowPlayingFragment.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
            val bottomSheetView = layoutInflater.inflate(R.layout.popup_dialog_queue, null)
            val recyclerView =
                bottomSheetView.findViewById<RecyclerView>(R.id.recyclerViewQueueActivity)
            val totalSongsTextView =
                bottomSheetView.findViewById<TextView>(R.id.totalSongsQueueActivity)

            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(13)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            musicAdapter =
                AdapterMusic(
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
            if (PlayerActivity.isPlaying) pauseMusic(PlayerActivity.musicService!!)
            else playMusic(PlayerActivity.musicService!!)
        }

        fragmentNowPlayingBottomBinding.NextMiniPlayer.setOnClickListener {
            prevNextSong(increment = true, PlayerActivity.musicService!!, viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            fragmentNowPlayingBottomBinding.root.visibility = View.VISIBLE

            Glide.with(this)
                .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].image)
                .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
                .into(fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)


            fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
            fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
                PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist
            if (PlayerActivity.isPlaying) fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(
                R.drawable.icon_pause
            )
            else fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_play)

        }
    }


}