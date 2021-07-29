package com.example.musicplayer.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.AlbumAdapter
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Album
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R

class AlbumFragment : Fragment() {
    companion object{
        var album_list = ArrayList<Album>()
    }
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false
    private var albumAdapter: AlbumAdapter? = null
    private var album_recyclerViews: RecyclerView? = null
    private var album_name_list = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        album_recyclerViews = view.findViewById(R.id.all_songs_album)
        album_recyclerViews?.setHasFixedSize(true);
        if(MainActivity.song_list.size>=1){
            setAlbumList()
            albumAdapter = AlbumAdapter(activity!!,  album_list)
            var layoutManager = GridLayoutManager(activity, 2)
            album_recyclerViews?.layoutManager = layoutManager
            album_recyclerViews?.adapter = albumAdapter

        }
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
    private fun setAlbumList(){
        album_list.clear()
        album_name_list.clear()
        for(musicItem in MainActivity.song_list){
            if(musicItem.album !in album_name_list) {
                album_name_list.add(musicItem.album)
                val albumItem = Album(musicItem.album, musicItem.path)
                album_list.add(albumItem)
            }
        }
    }
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player_album, miniPlayer!!)
            .commit()
    }

}