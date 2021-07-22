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
import com.example.musicplayer.Activities.ServiceCommunication
import com.example.musicplayer.Models.Song

class MusicService : Service() {
    var binder: IBinder =   MyBinder()
    var mediaPlayer: MediaPlayer? =null
    var position: Int = -1;
    var actionPlaying: ActionPlaying? =null
    var onMiniPlayerChangeListener: OnMiniPlayerChangeListener? = null
    var onPlaylistDetailClickListener: OnPlaylistDetailClickListener? = null
    private var sender: String? = null;
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
        sender = intent?.getStringExtra(ServiceCommunication.SENDER_ACTIVITY)
        if(intent?.getSerializableExtra(ServiceCommunication.GET_SONGS_LIST_ACTION)!=null) {
            musicFiles = intent?.getSerializableExtra(ServiceCommunication.GET_SONGS_LIST_ACTION) as ArrayList<Song>
        }

        var actionName = intent?.getStringExtra(ServiceCommunication.MEDIA_PLAYER_ACTION)
        if(intent!=null)  myPosition = intent.getIntExtra(ServiceCommunication.SERVICE_POSITION, -1)
        if(myPosition!=-1){

            playMedia(myPosition)
        }
        if(actionName!=null) {
            when(actionName) {
                ServiceCommunication.ACTION_PLAY-> {
                  //  Toast.makeText(this, "PlayPause", Toast.LENGTH_LONG).show()
                   actionPlaying?.playPause()
                   onPlaylistDetailClickListener?.playPause()
                   onMiniPlayerChangeListener?.playPause()

                }
                ServiceCommunication.ACTION_NEXT-> {
                  //  Toast.makeText(this, "Next", Toast.LENGTH_LONG).show()
                   actionPlaying?.playNext()
                    onPlaylistDetailClickListener?.playNext()
                    onMiniPlayerChangeListener?.playNext()

                }
                ServiceCommunication.ACTION_PREV-> {
                   // Toast.makeText(this, "Previous", Toast.LENGTH_LONG).show()
                   actionPlaying?.playPrev()
                    onPlaylistDetailClickListener?.playPrev()
                    onMiniPlayerChangeListener?.playPrev()

                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
    fun clickNext() {
        if(sender!=null){
            when(sender) {
                ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY -> onPlaylistDetailClickListener?.playNext()
                ServiceCommunication.PLAYER_ACTIVITY -> actionPlaying?.playNext()
            }
        }

    }
    fun clickPrev() {
        if(sender!=null){
            when(sender) {
                ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY -> onPlaylistDetailClickListener?.playPrev()
                ServiceCommunication.PLAYER_ACTIVITY -> actionPlaying?.playPrev()
            }
        }
    }
    fun clickPlay() {
        if(sender!=null){
            when(sender) {
                ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY -> onPlaylistDetailClickListener?.playPause()
                ServiceCommunication.PLAYER_ACTIVITY -> actionPlaying?.playPause()
            }
        }
    }
    private fun playMedia(startPosition: Int) {
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