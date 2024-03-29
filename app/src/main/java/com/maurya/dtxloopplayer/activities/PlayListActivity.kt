package com.maurya.dtxloopplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.ActivityPlaylistBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.checkListData
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.updateTextViewWithItemCount
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayListActivity : AppCompatActivity(),MediaControlInterface {

    private lateinit var binding: ActivityPlaylistBinding

    private lateinit var adapterMusic: AdapterMusic

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    companion object {
        var currentPlayListMusicList: ArrayList<MusicDataClass> = arrayListOf()
    }

    private var playListSongPreference: List<MusicDataClass> = listOf()
    private var currentPlayListUUID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        setRecyclerView()
        fetchSongsFromPlayList()
        listeners(currentPlayListUUID.toString())

    }

    private fun fetchSongsFromPlayList() {

        currentPlayListUUID = intent.getStringExtra("playListName")
        playListSongPreference =
            sharedPreferenceHelper.getPlayListSong(currentPlayListUUID.toString())

        if (playListSongPreference.isNotEmpty()) {
            binding.noSongsPlaylistActivity.visibility = View.GONE
            binding.recyclerViewPlayListActivity.visibility = View.VISIBLE
            binding.shuffle.visibility = View.VISIBLE
            currentPlayListMusicList.clear()
            currentPlayListMusicList.addAll(playListSongPreference)
            currentPlayListMusicList = checkListData(currentPlayListMusicList)
            adapterMusic.notifyDataSetChanged()
        } else {
            binding.recyclerViewPlayListActivity.visibility = View.GONE
            binding.shuffle.visibility = View.GONE
            binding.noSongsPlaylistActivity.visibility = View.VISIBLE
        }
    }

    private fun setRecyclerView() {
        binding.recyclerViewPlayListActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(this@PlayListActivity, LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                this@PlayListActivity,
                currentPlayListMusicList,
                this@PlayListActivity,
                sharedPreferenceHelper,
                playListActivity = true
            )
            adapter = adapterMusic
        }
    }

    private fun listeners(currentPlayListUUID: String) {

        changeItemCount()

        binding.playlistNamePlayListActivity.text = currentPlayListUUID

        binding.PlayListBackBtn.setOnClickListener { finish() }

        binding.addBtnPlayListActivity.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            intent.putExtra("playListName", currentPlayListUUID)
            startActivity(intent)
        }

        binding.shuffleBtnPlayListActivity.setOnClickListener {
            sendIntent(this@PlayListActivity, position = 0, reference = "PlayListActivityShuffle")
        }

        binding.removeAllBtnPlayListActivity.setOnClickListener {

            val alertDialog = MaterialAlertDialogBuilder(this)
            alertDialog.setTitle("Remove all")
                .setMessage("Are you sure you want to remove all songs from this playlist?")
                .setPositiveButton("OK") { dialog, _ ->
                    currentPlayListMusicList.clear()
                    sharedPreferenceHelper.savePlayListSong(
                        currentPlayListMusicList,
                        currentPlayListUUID
                    )
                    sharedPreferenceHelper.savePlayListSongCount(
                        currentPlayListMusicList.size,
                        currentPlayListUUID
                    )
                    adapterMusic.notifyDataSetChanged()
                    changeItemCount()
                    fetchSongsFromPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()

        }

    }

    private fun changeItemCount() {
        binding.totalSongsPlayListActivity.text =
            updateTextViewWithItemCount(currentPlayListMusicList.size)
    }

    override fun onResume() {
        super.onResume()
        adapterMusic.notifyDataSetChanged()
        changeItemCount()
        fetchSongsFromPlayList()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPlayListMusicList.clear()
    }

    override fun onSongSelected(musicList: ArrayList<MusicDataClass>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onAddToQueue(song: MusicDataClass) {
        TODO("Not yet implemented")
    }


}