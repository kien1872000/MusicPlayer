package com.example.musicplayer.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Activities.PlaylistDetailActivity
import com.example.musicplayer.Adapters.PlaylistDetailAdapter
import com.example.musicplayer.Adapters.SongSuggestAdapter
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnAcceptClickListener
import com.example.musicplayer.R
import com.example.musicplayer.SongSuggestClickListener
import kotlinx.android.synthetic.main.diaglog_song_suggest.*
import kotlinx.android.synthetic.main.song_suggest_item.*

class SongSuggestFragment(var onAcceptClick: OnAcceptClickListener?, var songsInPlaylist: ArrayList<Song>?): DialogFragment(), SongSuggestClickListener {
    private var songsSuggest = ArrayList<Song>()
    private var songAdapter: SongSuggestAdapter? = null
    private var checkList = ArrayList<Boolean>()
    private var songsSelected = ArrayList<Song>()
    private lateinit var dialogView: View
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            dialogView = LayoutInflater.from(context).inflate(R.layout.diaglog_song_suggest, null)

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
                        onAcceptClick!!.onClick(songsSelected)
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
            Log.d("kkkkk", "song_sugges: ${songsSuggest.size}, checkList: ${checkList.size}")
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initSongSuggests()
        songAdapter = SongSuggestAdapter(activity!!,  songsSuggest, this)
        var layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        song_suggests_listView?.layoutManager = layoutManager
        song_suggests_listView?.adapter = songAdapter
        check_img?.setColorFilter(Color.GRAY)
    }

    override fun onClick(position: Int) {
        if(!checkList[position]){
            checkList[position] = true
            var view = song_suggests_listView.layoutManager?.findViewByPosition(position)
            var checkImage = view?.findViewById(R.id.check_img) as ImageView
            checkImage.setColorFilter(Color.GREEN)
        }
        else{
            checkList[position] = false
            var view = song_suggests_listView.layoutManager?.findViewByPosition(position)
            var check_image = view?.findViewById(R.id.check_img) as ImageView
            check_image.setColorFilter(Color.GRAY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}