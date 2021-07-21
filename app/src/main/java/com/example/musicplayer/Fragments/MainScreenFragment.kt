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
import com.example.musicplayer.Adapters.CategoryAdapter
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Category
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
class MainScreenFragment : Fragment() {
    private var albumFragment : AlbumFragment? = null
    private var genresFragment :  GenresFragment? = null
    private var allSongFragment : AllSongFragment? = null
    private var playListFragment: PlaylistFragment? = null
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false;
    private var isFirst = true;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var categories : ArrayList<Category> = ArrayList()
        categories.add(Category("Album", R.drawable.music_album))
        categories.add(Category("Thể loại", R.drawable.boom_box))
        categories.add(Category("Bài hát", R.drawable.musical_note))
        categories.add(Category("Danh sách nghe", R.drawable.album))
        listView.adapter = CategoryAdapter(activity,R.layout.category, categories)
        listView.onItemClickListener = AdapterView.OnItemClickListener{
                parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            when(position){
                0 -> showAlbumFragment()
                1-> showGenresFragment()
                2 -> showAllSongFragment()
                else -> showPlaylistFragment()
            }
        }


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
}