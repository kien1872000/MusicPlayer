package com.example.musicplayer.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Adapters.AlbumAdapter
import com.example.musicplayer.Adapters.SongAdapter
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
}