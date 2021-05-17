package com.example.musicplayer.Activities

import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adapters.PlaylistDetailAdapter
import com.example.musicplayer.Fragments.SongSuggestFragment
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnAcceptClickListener
import com.example.musicplayer.OnSongClick
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_player.positionBar
import kotlinx.android.synthetic.main.activity_player.volumeBar
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.fragment_song_playing.*
import kotlinx.android.synthetic.main.song.*
import kotlinx.android.synthetic.main.song.song_image

class PlaylistDetailActivity : AppCompatActivity(), OnSongClick, OnAcceptClickListener {
    companion object{
        var playlist_songs = ArrayList<Song>()
    }
    private var position = 0
    private var mediaPlayer: MediaPlayer? = null
    private var play_list_name: String? = null
    private var flag = false
    private var isRepeat = false
    private var isShuffle = false
    private var uri: Uri? = null
    private var playlistDetailAdapter: PlaylistDetailAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        getIntentMethod()
        playlist_songs_listView?.setHasFixedSize(true)
//        if(playlist_songs.size>=1){
            playlistDetailAdapter = PlaylistDetailAdapter(this, playlist_songs, this)
            var layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            playlist_songs_listView?.layoutManager = layoutManager
            playlist_songs_listView?.adapter = playlistDetailAdapter
//       }
    }
    private fun getIntentMethod(){
        if(!flag){
            flag = true
            playlist_playButton.setImageResource(R.drawable.stop)
        }
        play_list_name = intent.getStringExtra("playlist_name")
        initPlaylist()
        if(!playlist_songs.isEmpty()){
            uri = Uri.parse(playlist_songs[0].path)
            setImage(uri.toString())
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            playlist_name_detail.text = playlist_songs[0].name
            mediaPlayer!!.start()
        }

    }
    private fun setImage(uri: String){
        val image = getSongArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap==null){
            playlist_image_detail.setImageResource(R.drawable.song_image)
        }
        else{
            playlist_image_detail.setImageBitmap(bitmap)
        }
    }
    private fun initPlaylist(){
        playlist_songs.clear()
        when(play_list_name){
            "Playlist 1" -> {
                playlist_songs.add(MainActivity.song_list[0])
                playlist_songs.add(MainActivity.song_list[1])
                playlist_songs.add(MainActivity.song_list[2])
                playlist_songs.add(MainActivity.song_list[3])
            }
            "Playlist 2"->{
                playlist_songs.add(MainActivity.song_list[4])
            }
        }
    }
    private fun getSongArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }
    override fun onClickItem(position: Int) {
        this.position = position
        if(mediaPlayer!=null&&mediaPlayer!!.isPlaying){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            uri = Uri.parse(playlist_songs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            setImage(uri.toString())
            playlist_playButton.setImageResource(R.drawable.stop)
            playlist_name_detail.text = playlist_songs[position].name
            mediaPlayer!!.start()
        }
        else{
            uri = Uri.parse(playlist_songs[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            setImage(uri.toString())
            playlist_playButton.setImageResource(R.drawable.stop)
            playlist_name_detail.text = playlist_songs[position].name
            mediaPlayer!!.start()
        }
    }
    private fun addSong(){
        addButton.setOnClickListener {
           showDialog()
        }
    }
    private fun playBtnClick(){
        playlist_playButton.setOnClickListener {
            if(mediaPlayer==null){
                Log.d("AAAAA", "1212")
                Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
            }
            else playPause()
        }
    }
    private fun autoNext(){
        mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    private fun prevBtnClick(){
        playlist_prevButton.setOnClickListener {
            if(mediaPlayer==null){
                Log.d("AAAAA", "1212")
                Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
            }
            else playPrev()
        }
    }
    private fun nextBtnClick(){
        playlist_nextButton.setOnClickListener {
            if(mediaPlayer==null){
                Log.d("AAAAA", "1212")
                Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
            }
            else playNext()
        }
    }
    private fun repeatBtnClick(){
        playlist_repeatButton.setOnClickListener {
            if(mediaPlayer==null){
                Log.d("AAAAA", "1212")
                Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
            }
            else playRepeat()
        }
    }
    private fun shuffleBtnClick(){
        playlist_shuffleButton.setOnClickListener {
            if(mediaPlayer==null){
                Log.d("AAAAA", "1212")
                Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
            }
            else playShuffle()
        }
    }
    private fun playNext(){
        if(!isRepeat){
            position = (position+1)% playlist_songs.size
            if(isShuffle){
                position = (position until playlist_songs.size).random()
            }
        }
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        playlist_name_detail.text = playlist_songs[position].name
        mediaPlayer!!.start()
        mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    private fun playPrev(){
        if(!isRepeat){
            position = if(position>0)  position -1 else playlist_songs.size-1
            if(isShuffle){
                position = (0..position).random()
            }
        }
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        playlist_name_detail.text = playlist_songs[position].name
        mediaPlayer!!.start()
    }
    private fun playRepeat(){
        if(!isRepeat){
            isRepeat = true;
            playlist_repeatButton.setColorFilter(Color.RED)
        }
        else{
            isRepeat = false
            playlist_repeatButton.setColorFilter(Color.BLACK)
        }

    }
    private fun playShuffle(){
        if(!isShuffle){
            isShuffle = true;
            playlist_shuffleButton.setColorFilter(Color.RED)
        }
        else{
            isShuffle = false
            playlist_shuffleButton.setColorFilter(Color.BLACK)
        }

    }
    private fun playPause(){
        if(mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
            playlist_playButton.setImageResource(R.drawable.play)
        }
        else{
            mediaPlayer!!.start()
            playlist_playButton.setImageResource(R.drawable.stop)
        }

    }
    override fun onResume() {
        super.onResume()
        shuffleBtnClick()
        repeatBtnClick()
        prevBtnClick()
        nextBtnClick()
        playBtnClick()
        autoNext()
        addSong()
    }
    private fun showDialog(){
        val newFragment = SongSuggestFragment(this)
        newFragment.show(supportFragmentManager, "add song dialog")
    }

    override fun onClick() {
        var playlist_song_temps = ArrayList<Song>()
        playlist_song_temps.addAll(playlist_songs)
        playlistDetailAdapter!!.songs.clear()
        playlistDetailAdapter = PlaylistDetailAdapter(this, playlist_song_temps, this)
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        playlist_songs_listView?.layoutManager = layoutManager
        playlist_songs_listView?.adapter = playlistDetailAdapter
        playlistDetailAdapter!!.notifyDataSetChanged()
        playlist_songs.addAll(playlist_song_temps)
        if(!playlist_songs.isEmpty()){
            if(mediaPlayer==null){
                uri = Uri.parse(playlist_songs[0].path)
                playlist_name_detail.text = playlist_songs[0].name
                setImage(uri.toString())
                mediaPlayer = MediaPlayer.create(applicationContext, uri)
                playlist_playButton.setImageResource(R.drawable.stop)
                mediaPlayer!!.start()
            }
        }
    }
}