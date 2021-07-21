package com.example.musicplayer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.musicplayer.Activities.MainActivity
import com.example.musicplayer.Activities.PlaylistDetailActivity
import com.example.musicplayer.Models.Song

class MusicService : Service() {
    var binder: IBinder =   MyBinder()
    var mediaPlayer: MediaPlayer? =null
    var position: Int = -1;
    var actionPlaying: ActionPlaying? =null
    var onMiniPlayerChangeListener: OnMiniPlayerChangeListener? = null;
    var onPlaylistDetailClickListener: OnPlaylistDetailClickListener? = null;
    inner class MyBinder : Binder() {
        fun getService() : MusicService? {
            return this@MusicService
        }
    }
    var musicFiles = ArrayList<Song>()
    var uri: Uri? = null

    override fun onCreate() {
        super.onCreate()
        musicFiles = MainActivity.song_list
    }
    override fun onBind(intent: Intent?): IBinder? {
        Log.e("Bind", "Method")
        return binder;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var myPosition: Int = -1;
        var actionName = intent?.getStringExtra("ActionName")
        if(intent!=null)  myPosition = intent.getIntExtra("servicePosition", -1)
        if(myPosition!=-1){
            Log.d("v111", myPosition.toString())
            playMedia(myPosition)
        }
        if(actionName!=null) {
            when(actionName) {
                "playPause"-> {
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_LONG).show()
                    actionPlaying?.playPause()
                    onMiniPlayerChangeListener?.playPause()
                    onPlaylistDetailClickListener?.playPause()
                }
                "next"-> {
                    Toast.makeText(this, "Next", Toast.LENGTH_LONG).show()
                    actionPlaying?.playNext()
                    onMiniPlayerChangeListener?.playNext()
                    onPlaylistDetailClickListener?.playNext()
                }
                "previous"-> {
                    Toast.makeText(this, "Previous", Toast.LENGTH_LONG).show()
                    actionPlaying?.playPrev()
                    onMiniPlayerChangeListener?.playPrev()
                    onPlaylistDetailClickListener?.playPrev()
                }
            }
        }
        return START_STICKY
    }
    fun clickNext() {
        actionPlaying?.playNext()
    }
    fun clickPrev() {
        actionPlaying?.playPrev()
    }
    fun clickPlay() {
        actionPlaying?.playPause()
    }
    private fun playMedia(startPosition: Int) {
        musicFiles = PlaylistDetailActivity.playlist_songs
        position = startPosition
        if(mediaPlayer!=null){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            if(musicFiles.size>0){
                createMediaPlayer(position)
                mediaPlayer!!.start()
            }
        }
        else{
            if(musicFiles.size>0){
                createMediaPlayer(position)
                mediaPlayer!!.start()
            }

        }

    }

    fun start(){
        mediaPlayer!!.start()
    }
    fun isPlaying(): Boolean{
       return mediaPlayer!!.isPlaying
    }
    fun pause(){
        mediaPlayer!!.pause()
    }
    fun setVolume(leftVolume: Float, rightVolume: Float){
        mediaPlayer!!.setVolume(leftVolume, rightVolume)
    }
    fun stop() {
        mediaPlayer!!.stop()
    }
    fun release(){
        mediaPlayer!!.release()
    }
    fun reset(){
        mediaPlayer!!.reset()
    }
    fun getDuration(): Int{
        return mediaPlayer!!.duration
    }
    fun getCurrentPosition(): Int {
        return mediaPlayer!!.currentPosition
    }
    fun seekTo(position: Int){
        mediaPlayer!!.seekTo(position)
    }
    fun createMediaPlayer(position: Int){
        uri = Uri.parse(musicFiles[position].path)
        MiniPlayer.PLAY_PAUSE = "Play"
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putString(MiniPlayer.MUSIC_FILE, uri.toString())
        editor?.putString(MiniPlayer.SONG_NAME, musicFiles[position].name)
        editor?.putString(MiniPlayer.SONG_ARTIST, musicFiles[position].artist)
        editor?.putBoolean(MiniPlayer.START_PLAYER_ACTIVITY, true)
        editor?.apply();
        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }
    fun createMediaPlayerWithPath(path: String) {
        var uriPath = Uri.parse(path)
        mediaPlayer = MediaPlayer.create(baseContext, uriPath)
    }

    fun setCallBack(actionPlaying: ActionPlaying){
        this.actionPlaying = actionPlaying
    }
    fun setOtherCallBack(onMiniPlayerChangeListener: OnMiniPlayerChangeListener?){
        this.onMiniPlayerChangeListener= onMiniPlayerChangeListener
    }
    fun setOnPlaylistDetailClick(onPlaylistDetailClickListener: OnPlaylistDetailClickListener?) {
        this.onPlaylistDetailClickListener = onPlaylistDetailClickListener
    }
}