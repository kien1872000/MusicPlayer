package com.example.musicplayer.Activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Adapters.SongAdapter
import com.example.musicplayer.Fragments.AlbumFragment
import com.example.musicplayer.Fragments.MiniPlayerFragment
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnSongClick
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.activity_album_detail.*


class AlbumDetailActivity : AppCompatActivity() {
    private var songAdapter: SongAdapter? = null
    private var position = -1
    private var miniPlayer: MiniPlayerFragment? = null
    private var isStart = false
    companion object{
        var songs= ArrayList<Song>()
    }
    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        album_detail_backButton.setColorFilter(Color.parseColor("#d7dade"))
        getIntentMethod()
        album_songs_listView?.setHasFixedSize(true);
        if (songs.size >= 1) {
            songAdapter = SongAdapter(this, songs, "albumDetail")
            var layoutManager: LinearLayoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            album_songs_listView?.layoutManager = layoutManager
            album_songs_listView?.adapter = songAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        var preferences: SharedPreferences = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
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
    private fun getIntentMethod(){
        position = intent.getIntExtra("position", -1)
        uri = Uri.parse(AlbumFragment.album_list[position].path)
        var image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            album_image_detail.setImageBitmap(bitmap)
        }
        album_name_detail.text = AlbumFragment.album_list[position].name
        for(songItem in MainActivity.song_list){
            if(songItem.album==AlbumFragment.album_list[position].name) songs.add(songItem)
        }
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
    private fun showMiniPlayer(){
        miniPlayer = MiniPlayerFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_bottom_player_album_detail, miniPlayer!!)
            .commit()
    }
}