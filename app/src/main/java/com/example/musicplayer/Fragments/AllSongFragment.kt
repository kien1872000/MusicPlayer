package com.example.musicplayer.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Adapters.SongAdapter
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.R
import com.example.musicplayer.Activities.MainActivity.Companion as MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AllSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllSongFragment : Fragment() {
    private var songAdapter: SongAdapter? = null
    private var all_song_recyclerView: RecyclerView? = null
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView = inflater.inflate(R.layout.fragment_all_song, container, false)
        all_song_recyclerView = rootView.findViewById(R.id.all_songs_listView)
        all_song_recyclerView?.setHasFixedSize(true);
        if(MainActivity.song_list.size>=1){
            songAdapter = SongAdapter(activity!!,  MainActivity.song_list, "allSongs")
            var layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            all_song_recyclerView?.layoutManager = layoutManager
            all_song_recyclerView?.adapter = songAdapter
        }

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var preferences: SharedPreferences = activity!!.
        getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
//        var value = preferences.getString(MiniPlayer.MUSIC_FILE, null)
//        Log.d("Hello mother fucker", value.toString())
//        if(value!=null) {
//            MiniPlayer.IS_SHOW_MINI_PLAYER = true;
//            MiniPlayer.PATH_TO_FRAG = value.toString();
//        }
//        else {
//            MiniPlayer.IS_SHOW_MINI_PLAYER = false;
//            MiniPlayer.PATH_TO_FRAG = null;
//        }
    }
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player, miniPlayer!!)
            .commit()
    }
    override fun onResume() {
        super.onResume()
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
}