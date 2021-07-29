package com.example.musicplayer.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.size
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.*
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.AlbumAdapter
import com.example.musicplayer.Adapters.PlaylistAdapter
import com.example.musicplayer.Models.Album
import com.example.musicplayer.Models.Playlist
import com.example.musicplayer.Models.Song
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_playlist.*


class PlaylistFragment : Fragment(), OnPlaylistItemChangeListener, OnCreatePlaylistAcceptListener {
    private var playlists = ArrayList<Playlist>()
    private var playlistAdapter: PlaylistAdapter? = null
    private var playlist_recyclerView: RecyclerView? = null
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlist_recyclerView= view.findViewById(R.id.all_songs_playlist)
        if(MainActivity.song_list.size>=1) {
            setPlaylistList()
            playlistAdapter = PlaylistAdapter(activity!!, playlists)
            var layoutManager = LinearLayoutManager(activity)
            playlist_recyclerView!!.layoutManager = layoutManager
            playlist_recyclerView!!.adapter = playlistAdapter
            playlist_recyclerView!!.setHasFixedSize(true)
        }
    }

    private fun addButtonClick() {
        add_playlist_button.setOnClickListener {
            showDialog()
        }
    }
    private fun setPlaylistList(){
        playlists = MainActivity.musicPlayerDbHelper!!.getAllPlaylists()


    }

    override fun onDeletePlaylistItem(position: Int) {
        playlists = MainActivity.musicPlayerDbHelper!!.getAllPlaylists()

    }

    override fun onAddPlaylistItem() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        addButtonClick()
        val newPlaylists = MainActivity.musicPlayerDbHelper!!.getAllPlaylists()
        if(playlists.size>newPlaylists.size) {
            playlists  = newPlaylists
            playlistAdapter = PlaylistAdapter(activity!!, playlists)
            playlist_recyclerView!!.adapter = playlistAdapter
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
        Log.d("khello", MainActivity.musicPlayerDbHelper!!.getAllPlaylists().size.toString() )
    }

    override fun onAccept(songs: ArrayList<Song>, playlistName: String) {
        playlistName?.trim()
        if(playlistName.isNullOrEmpty()) {
            Toast.makeText(activity, "Phải nhập tên của playlist", Toast.LENGTH_SHORT).show()
        }
        else {
            if(playlists.any { it.name==playlistName }) {
                Toast.makeText(activity, "Tên của playlist này đã tồn tại", Toast.LENGTH_SHORT).show()
            }
            else{
                if(songs.isNullOrEmpty()) {
                    Toast.makeText(activity, "Bạn chưa chọn bài hát nào", Toast.LENGTH_SHORT).show()
                }
                else {
                    val position = playlists.size
                    val playlistId =  MainActivity.musicPlayerDbHelper!!.addPlaylist(playlistName, songs)
                    playlists.add(Playlist(playlistId, -1, playlistName))
                    playlistAdapter!!.notifyItemInserted(position)
                }

            }
        }

    }
    private fun showDialog(){
        val newFragment = PlaylistCreationFragment(this, null)
        newFragment.show(activity!!.supportFragmentManager, "create playlist dialog")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        playlistAdapter = null
        playlist_recyclerView!!.adapter = null
        playlist_recyclerView = null
    }
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player_playlist, miniPlayer!!)
            .commit()
    }
}