package com.example.musicplayer.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.size
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.AlbumAdapter
import com.example.musicplayer.Adapters.CategoryAdapter
import com.example.musicplayer.Adapters.RecentSongsAdapter
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Category
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnClickCategoryItemListener
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.fragment_main_screen.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainScreenFragment : Fragment(), OnClickCategoryItemListener {
    private var albumFragment : AlbumFragment? = null
    private var genresFragment :  GenresFragment? = null
    private var allSongFragment : AllSongFragment? = null
    private var playListFragment: PlaylistFragment? = null
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false;
    private var recentSongsAdapter: RecentSongsAdapter? = null
    private var listenALotSongsAdapter: RecentSongsAdapter? = null
    private var categoryAdapter: CategoryAdapter? = null
    private var isFirst = true
    companion object {
        var recentSongList: ArrayList<Song> = ArrayList()
        var listenALotSongList: ArrayList<Song> = ArrayList()
        var categories: ArrayList<Category> = ArrayList()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initCategories()
        initListenALotSongs()
        initRecentSongs()
    }
    private fun initCategories() {
        categoryAdapter = CategoryAdapter(activity!!, categories, this)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        category_list!!.layoutManager = layoutManager
        category_list!!.adapter = categoryAdapter
        category_list!!.setHasFixedSize(true)
    }
    private fun initData() {
        if(isFirst) {
            categories.add(Category("Album", R.drawable.music_album))
            categories.add(Category("Thể loại", R.drawable.boom_box))
            categories.add(Category("Bài hát", R.drawable.musical_note))
            categories.add(Category("Danh sách nghe", R.drawable.album))
            recentSongList.add(MainActivity.song_list[0])
            recentSongList.add(MainActivity.song_list[1])
            recentSongList.add(MainActivity.song_list[2])
            recentSongList.add(MainActivity.song_list[3])
            recentSongList.add(MainActivity.song_list[4])
            listenALotSongList.add(MainActivity.song_list[0])
            listenALotSongList.add(MainActivity.song_list[1])
            listenALotSongList.add(MainActivity.song_list[2])
            listenALotSongList.add(MainActivity.song_list[3])
            isFirst = false;
        }
    }
    private fun initRecentSongs(){
        recentSongsAdapter= RecentSongsAdapter(activity!!,  recentSongList)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recent_list!!.layoutManager = layoutManager
        recent_list!!.adapter = recentSongsAdapter
        recent_list!!.setHasFixedSize(true)
    }
    private fun initListenALotSongs() {
        listenALotSongsAdapter= RecentSongsAdapter(activity!!, listenALotSongList)
        val layoutManager = GridLayoutManager(activity, 2)
        listen_a_lot_list!!.layoutManager = layoutManager
        listen_a_lot_list!!.adapter = listenALotSongsAdapter
    }
    private fun showAlbumFragment(){
        albumFragment = AlbumFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.main_id, albumFragment!!).addToBackStack(null)
            .commit();
    }
    private fun showPlaylistFragment(){
        playListFragment = PlaylistFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.main_id, playListFragment!!).addToBackStack(null)
            .commit();
    }
    private fun showGenresFragment(){
        genresFragment = GenresFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.main_id, genresFragment!!).addToBackStack(null)
            .commit();
    }
    private fun showAllSongFragment(){
        allSongFragment = AllSongFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.main_id,allSongFragment!!).addToBackStack(null)
            .commit();
    }
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player_main_screen, miniPlayer!!)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        var preferences: SharedPreferences = activity!!.
        getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
        val path = preferences.getString(MiniPlayer.MUSIC_FILE, null)
        val songName = preferences.getString(MiniPlayer.SONG_NAME, null)
        val songArtist = preferences.getString(MiniPlayer.SONG_ARTIST, null)
        isStart = preferences.getBoolean(MiniPlayer.START_PLAYER_ACTIVITY, false);
        if(isStart) {
            showMiniPlayer()
        }
        if(path!=null) {
            MiniPlayer.IS_SHOW_MINI_PLAYER = true
            MiniPlayer.PATH_TO_FRAG = path
            MiniPlayer.SONG_ARTIST_TO_FRAG =  songArtist
            MiniPlayer.SONG_NAME_TO_FRAG = songName
            MiniPlayer.SONG_ARTIST_TO_FRAG = songArtist
        }
        else{
            MiniPlayer.IS_SHOW_MINI_PLAYER = false;
            MiniPlayer.PATH_TO_FRAG = null;
            MiniPlayer.SONG_ARTIST_TO_FRAG = null
            MiniPlayer.SONG_NAME_TO_FRAG = null
        }
    }

    override fun onClickCategoryItem(position: Int) {
        when(position){
            0 -> showAlbumFragment()
            1-> showGenresFragment()
            2 -> showAllSongFragment()
            else -> showPlaylistFragment()
        }
    }
}