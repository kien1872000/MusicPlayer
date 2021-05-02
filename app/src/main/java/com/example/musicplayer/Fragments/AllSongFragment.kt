package com.example.musicplayer.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.SongAdapter
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.fragment_all_song.*
import kotlinx.android.synthetic.main.fragment_main_screen.*
import kotlinx.android.synthetic.main.song.*

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
    private var songPlayingFragment : SongPlayingFragment? = null
    private var albumFragment: AlbumFragment? = null
    private var songAdapter: SongAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(MainActivity.song_list.size>=1){
            if(all_songs_listView!=null){
                all_songs_listView.adapter = SongAdapter(activity,R.layout.song, MainActivity.song_list)
            }
        }
//        if(!MainActivity.song_list.size<1){
//            songAdapter =  SongAdapter(activity,R.layout.song, MainActivity.song_list)
//            all_songs_listView.adapter = songAdapter
//        }
//        var songs : ArrayList<Song> = ArrayList()
////        songs.add(Song("Song 1", R.drawable.song_image, ""))
////        songs.add(Song("Song 2", R.drawable.song_image, ""))
////        songs.add(Song("Song 3", R.drawable.song_image, ""))
////        songs.add(Song("Danh sách nghedsfdsfsdfdsfsdfsdfsdf", R.drawable.song_image,""))
//        all_songs_listView.adapter = SongAdapter(activity,R.layout.song, songs)
//        all_songs_listView.onItemClickListener = AdapterView.OnItemClickListener{
//                parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
//            when(position){
//                0 -> playSong()
//                1-> showAlbumFragment()
//                2 -> Toast.makeText(activity, "Nothing to show", Toast.LENGTH_LONG).show()
//                else -> Toast.makeText(activity, "Nothing to show", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//    private fun playSong(){
//        if(songPlayingFragment!=null){
//            activity!!.supportFragmentManager.beginTransaction().
//            replace(R.id.main_id, songPlayingFragment!!)
//                    .addToBackStack(null).commit()
//        }
//        songPlayingFragment = SongPlayingFragment()
//        activity!!.supportFragmentManager.beginTransaction().
//        replace(R.id.main_id, songPlayingFragment!!).
//                addToBackStack(null).commit()
//    }
//    private fun showAlbumFragment(){
//        albumFragment = AlbumFragment()
//        activity!!.supportFragmentManager.beginTransaction()
//            .replace(R.id.main_id, albumFragment!!).addToBackStack(null)
//            .commit();
    }
}