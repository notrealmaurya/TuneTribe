package com.maurya.dtxloopplayer.activities

import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.dtxloopplayer.adapter.MusicAdapter
import com.maurya.dtxloopplayer.dataEntity.MusicData
import com.maurya.dtxloopplayer.databinding.ActivityFolderTracksActiivityBinding
import java.io.File
import android.util.Base64
import com.maurya.dtxloopplayer.dataEntity.updateTextViewWithItemCount


class FolderTracksActivity : AppCompatActivity() {


    companion object {
        var folderMusicFiles = ArrayList<MusicData>()
        lateinit var binding: ActivityFolderTracksActiivityBinding
        lateinit var musicAdapter: MusicAdapter
        var isInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderTracksActiivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isInitialized = true
        val folderPath = intent.getStringExtra("folderPath") ?: ""
        folderMusicFiles = listMusicFilesInFolder(folderPath)


        binding.recyclerViewFoldersTrackActivity.setItemViewCacheSize(10)
        binding.recyclerViewFoldersTrackActivity.setHasFixedSize(true)
        binding.recyclerViewFoldersTrackActivity.layoutManager = LinearLayoutManager(this)
        musicAdapter =
            MusicAdapter(this, folderMusicFiles, folderSongsActivity = true)
        binding.recyclerViewFoldersTrackActivity.adapter = musicAdapter

        updateTextViewWithItemCount(musicAdapter, binding.totalSongsFoldersTrackActivity)

        val folderName = File(folderPath).name
        binding.foldersNameFoldersTrackActivity.text = folderName


        listeners()

    }

    private fun listeners() {

        binding.folderTracksBackBtn.setOnClickListener {
            finish()
        }

        binding.shuffleBtnFoldersTrackActivity.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "folderSongsActivityShuffle")
            startActivity(intent)
        }


    }

    private fun listMusicFilesInFolder(folderPath: String): ArrayList<MusicData> {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            return ArrayList()
        }

        val musicFileExtensions = listOf(".mp3", ".wav", ".ogg", ".flac")
        val musicFiles = ArrayList<MusicData>()

        val folderFiles = folder.listFiles() ?: return ArrayList()

        for (file in folderFiles) {
            if (file.isFile) {
                val fileName = file.name.toLowerCase()
                if (musicFileExtensions.any { fileName.endsWith(it) }) {
                    val mediaMetadataRetriever = MediaMetadataRetriever().apply {
                        setDataSource(file.absolutePath)
                    }

                    val idFTA = file.absolutePath
                    val titleFTA =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            ?.let { it } ?: file.name
                    val albumFTA =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                            ?: ""
                    val artistFTA =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                            ?: ""
                    val durationFTA =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong() ?: 0

                    val artByteArray = mediaMetadataRetriever.getEmbeddedPicture()
                    val artUriFTA = artByteArray?.let {
                        val base64Image = Base64.encodeToString(it, Base64.DEFAULT)
                        "data:image/jpeg;base64,$base64Image"
                    } ?: ""

                    val dateFTA =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                            ?.toLong() ?: 0

                    val musicItem = MusicData(
                        id = idFTA,
                        title = titleFTA,
                        album = albumFTA,
                        artist = artistFTA,
                        duration = durationFTA,
                        path = file.absolutePath,
                        artUri = artUriFTA,
                        dateModified = dateFTA
                    )
                    musicFiles.add(musicItem)
                }
            }
        }
        return musicFiles
    }


}