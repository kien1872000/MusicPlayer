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

class SongSuggestFragment(var onAcceptClick: OnAcceptClickListener?): DialogFragment(), SongSuggestClickListener {
    private var song_suggests = ArrayList<Song>()
    private var songAdapter: SongSuggestAdapter? = null
    private var checkList = ArrayList<Boolean>()
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
                        for(i in 0..checkList.size-1){
                            if(checkList[i]) PlaylistDetailActivity.playlist_songs.add(song_suggests[i])
                        }
                        onAcceptClick!!.onClick()
                    })
                .setNegativeButton("Hủy",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    private fun initSongSuggests(){
        if(PlaylistDetailActivity.playlist_songs.isEmpty()){
            song_suggests.addAll(MainActivity.song_list)
            for(index in 0 until (song_suggests.size)) checkList.add(false)
            Log.d("kkkkk", "song_sugges: ${song_suggests.size}, checkList: ${checkList.size}")
        }
        else{
            song_suggests.clear()
            checkList.clear()
            for(song in MainActivity.song_list){
                if(song !in PlaylistDetailActivity.playlist_songs)  song_suggests.add(song)
            }
            for(index in 0 until (song_suggests.size) ) {checkList.add(false)}
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initSongSuggests()
        songAdapter = SongSuggestAdapter(activity!!,  song_suggests, this)
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
            var check_image = view?.findViewById(R.id.check_img) as ImageView
            check_image.setColorFilter(Color.GREEN)
        }
        else{
            checkList[position] = false
            var view = song_suggests_listView.layoutManager?.findViewByPosition(position)
            var check_image = view?.findViewById(R.id.check_img) as ImageView
            check_image.setColorFilter(Color.GRAY)
        }
    }

    override fun onDestroy() {
        Log.d("kkkkk", "121212")
        super.onDestroy()
    }
}