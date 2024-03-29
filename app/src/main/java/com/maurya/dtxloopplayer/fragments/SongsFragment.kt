package com.maurya.dtxloopplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.R
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.databinding.FragmentSongsBinding
import com.maurya.dtxloopplayer.utils.MediaControlInterface
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.sendIntent
import com.maurya.dtxloopplayer.utils.showToast
import com.maurya.dtxloopplayer.utils.sortMusicList
import com.maurya.dtxloopplayer.viewModelsObserver.ModelResult
import com.maurya.dtxloopplayer.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SongsFragment : Fragment(), MediaControlInterface {


    private lateinit var fragmentSongsBinding: FragmentSongsBinding

    private lateinit var adapterMusic: AdapterMusic

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper
    private var sortingOrder: String = ""

    private val viewModel: ViewModelObserver by viewModels()

    private val sortOptions = arrayOf(
        "DISPLAY_NAME ASC",
        "DISPLAY_NAME DESC",
        "SIZE DESC",
        "SIZE ASC",
        "DATE_ADDED DESC",
        "DATE_ADDED ASC"
    )

    companion object {
        var musicList: ArrayList<MusicDataClass> = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentSongsBinding = FragmentSongsBinding.inflate(inflater, container, false)
        val view = fragmentSongsBinding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesHelper = SharedPreferenceHelper(requireContext())
        sortingOrder = sharedPreferencesHelper.getSortingOrder().toString()

        fragmentSongsBinding.recyclerViewSongFragment.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterMusic = AdapterMusic(
                requireContext(),
                musicList,
                this@SongsFragment
            )
            adapter = adapterMusic
        }

        fetchMusicUsingViewModel()

        listener()


    }


    private fun fetchMusicUsingViewModel() {
        viewModel.fetchSongs(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songsStateFLow.collect {
                    when (it) {
                        is ModelResult.Success -> {
                            fragmentSongsBinding.progressBar.visibility = View.GONE
                            musicList.clear()
                            musicList.addAll(it.data!!)
                            fragmentSongsBinding.MusicListTotalSongFragment.text =
                                "${musicList.size} songs"
                            sortMusicList(sortingOrder, musicList, adapterMusic)
                        }

                        is ModelResult.Error -> {
                            fragmentSongsBinding.progressBar.visibility = View.GONE
                            showToast(requireContext(), it.message.toString())
                        }

                        is ModelResult.Loading -> {
                            fragmentSongsBinding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun listener() {

        fragmentSongsBinding.shuffleBtnSongFragment.setOnClickListener {
            sendIntent(requireContext(), reference = "SongsFragmentShuffle", position = 0)
        }

        fragmentSongsBinding.sortingVideoFragment.setOnClickListener {
            showSortingMenu()
        }

    }

    private fun showSortingMenu() {
        val inflater =
            requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_home_menu, null)

        val wid = LinearLayout.LayoutParams.WRAP_CONTENT
        val high = LinearLayout.LayoutParams.WRAP_CONTENT
        val focus = true
        val popupWindow = PopupWindow(popupView, wid, high, focus)

        val location = IntArray(2)
        fragmentSongsBinding.sortingVideoFragment.getLocationOnScreen(location)
        val x = location[0] + fragmentSongsBinding.sortingVideoFragment.width
        val y = location[1] + fragmentSongsBinding.sortingVideoFragment.height


        popupWindow.showAtLocation(fragmentSongsBinding.root, Gravity.NO_GRAVITY, x, y)


        val layoutIds = arrayOf(
            R.id.nameAtoZLayoutPopUpMenu,
            R.id.nameZtoALayoutPopUpMenu,
            R.id.largestFirstLayoutPopUpMenu,
            R.id.smallestFirstLayoutPopUpMenu,
            R.id.newestFirstLayoutPopUpMenu,
            R.id.oldestFirstLayoutPopUpMenu
        )

        layoutIds.forEachIndexed { index, layoutId ->
            val layout = popupView.findViewById<LinearLayout>(layoutId)
            layout.setOnClickListener {
                sortMusicList(sortOptions[index], musicList, adapterMusic)
                sharedPreferencesHelper.saveSortingOrder(sortOptions[index])
                popupWindow.dismiss()
            }
        }


    }

    override fun onSongSelected(songList: ArrayList<MusicDataClass>, position: Int) {
        if (activity is MainActivity) {
            (activity as MainActivity).onSongSelected(songList, position)
        }

    }

    override fun onAddToQueue(song: MusicDataClass) {


    }


}
