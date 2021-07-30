package com.example.musicplayer.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.FavoriteAdapter
import com.example.musicplayer.Adapters.SongAdapter
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.fragment_favorite.*


class FavoriteFragment : Fragment() {
    private var favoriteAdapter: FavoriteAdapter? = null
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false
    private var favoriteSongsList = ArrayList<Song>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)


        return view
    }

    override fun onResume() {
        super.onResume()
        favoriteSongsList = MainActivity.musicPlayerDbHelper!!.getAllFavoriteSongs()
        if(favoriteSongsList.size>0) {
            favoriteAdapter = FavoriteAdapter(activity!!,  favoriteSongsList, "favorite")
            var layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            favorite_list!!.layoutManager = layoutManager
            favorite_list!!.adapter = favoriteAdapter
        }
        var preferences: SharedPreferences = activity!!.
        getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
        val path = preferences.getString(MiniPlayer.MUSIC_FILE, null)
        val songName = preferences.getString(MiniPlayer.SONG_NAME, null)
        val songArtist = preferences.getString(MiniPlayer.SONG_ARTIST, null)
        isStart = preferences.getBoolean(MiniPlayer.START_PLAYER_ACTIVITY, false)
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
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player_favorite, miniPlayer!!)
            .commit()
    }

}