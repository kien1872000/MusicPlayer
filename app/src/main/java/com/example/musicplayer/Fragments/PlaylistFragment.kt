package com.example.musicplayer.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.AlbumAdapter
import com.example.musicplayer.Adapters.PlaylistAdapter
import com.example.musicplayer.Models.Album
import com.example.musicplayer.R


class PlaylistFragment : Fragment() {
    companion object{
        var playlist_list = ArrayList<Album>()
    }
    private var playlistAdapter: PlaylistAdapter? = null
    private var playlist_recyclerView: RecyclerView? = null


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
        Log.d("TTTT", R.id.all_songs_playlist.toString())
        playlist_recyclerView?.setHasFixedSize(true);
        if(MainActivity.song_list.size>=1) {
            setPlaylistList()
            playlistAdapter = PlaylistAdapter(activity!!, playlist_list)
            var layoutManager = GridLayoutManager(activity, 2)
            playlist_recyclerView?.layoutManager = layoutManager
            playlist_recyclerView?.adapter = playlistAdapter
        }
    }
    private fun setPlaylistList(){
        playlist_list.clear()
        playlist_list.add(Album("Playlist 1", ""))
        playlist_list.add(Album("Playlist 2", ""))
        playlist_list.add(Album("Playlist 3", ""))
        playlist_list.add(Album("Playlist 4", ""))
    }

}