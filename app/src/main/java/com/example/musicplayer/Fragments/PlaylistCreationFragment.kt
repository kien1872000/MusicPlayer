package com.example.musicplayer.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Adapters.SongSuggestAdapter
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnAcceptClickListener
import com.example.musicplayer.OnCreatePlaylistAcceptListener
import com.example.musicplayer.R
import com.example.musicplayer.SongSuggestClickListener
import kotlinx.android.synthetic.main.diaglog_song_suggest.*
import kotlinx.android.synthetic.main.dialog_playlist_creation.*
import kotlinx.android.synthetic.main.song_suggest_item.*

class PlaylistCreationFragment (var onAcceptClick: OnCreatePlaylistAcceptListener?, var songsInPlaylist: ArrayList<Song>?): DialogFragment(),
    SongSuggestClickListener {
    private var songsSuggest = ArrayList<Song>()
    private var songAdapter: SongSuggestAdapter? = null
    private var checkList = ArrayList<Boolean>()
    private var songsSelected = ArrayList<Song>()
    private var playlistName: String? = null
    private lateinit var dialogView: View

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.setWindowAnimations(
            R.style.DialogAnimation)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_playlist_creation, null)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Thêm",
                    DialogInterface.OnClickListener { dialog, id ->
                        songsSelected.clear()
                        for(i in 0 until checkList.size) {
                            if(checkList[i]) songsSelected.add(songsSuggest[i])
                        }
                        playlistName = playlist_name_edt.text.toString()
                        onAcceptClick!!.onAccept(songsSelected, playlistName.toString())
                    })
                .setNegativeButton("Hủy",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    private fun initSongSuggests(){
        songsSuggest.clear()
        checkList.clear()
        if(songsInPlaylist.isNullOrEmpty()){
            songsSuggest.addAll(MainActivity.song_list)
            for(index in 0 until (songsSuggest.size)) checkList.add(false)
        }
        else{
            for(song in MainActivity.song_list){
                if(!songsInPlaylist.isNullOrEmpty()) {
                    if(!songsInPlaylist!!.any{ it.path == song.path }) {
                        songsSuggest.add(song)
                    }
                }
            }
            for(index in 0 until (songsSuggest.size) ) {checkList.add(false)}
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initSongSuggests()
        songAdapter = SongSuggestAdapter(activity!!,  songsSuggest, this, 1)
        var layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        song_suggests_playlist_creation_list!!.layoutManager = layoutManager
        song_suggests_playlist_creation_list!!.adapter = songAdapter
        check_img?.setColorFilter(Color.GRAY)
    }

    override fun onClick(position: Int) {
        if(!checkList[position]){
            checkList[position] = true
            var view = song_suggests_playlist_creation_list!!.layoutManager!!.findViewByPosition(position)
            var checkImage = view?.findViewById(R.id.check_img) as ImageView
            checkImage.setColorFilter(Color.GREEN)
        }
        else{
            checkList[position] = false
            var view = song_suggests_playlist_creation_list!!.layoutManager!!.findViewByPosition(position)
            var check_image = view?.findViewById(R.id.check_img) as ImageView
            check_image.setColorFilter(Color.GRAY)
        }
    }

}