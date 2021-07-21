package com.example.musicplayer.Activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import com.example.musicplayer.Adapters.PlaylistDetailAdapter
import com.example.musicplayer.Fragments.SongSuggestFragment
import com.example.musicplayer.Models.Song
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_player.positionBar
import kotlinx.android.synthetic.main.activity_player.volumeBar
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.fragment_song_playing.*
import kotlinx.android.synthetic.main.song.*
import kotlinx.android.synthetic.main.song.song_image
import kotlin.math.log

class PlaylistDetailActivity : AppCompatActivity(), OnSongClick, OnAcceptClickListener, ServiceConnection, OnPlaylistDetailClickListener {
    companion object{
        var playlist_songs = ArrayList<Song>()
    }
    private var position = 0
   // private var mediaPlayer: MediaPlayer? = null
    private var play_list_name: String? = null
    private var flag = false
    private var isRepeat = false
    private var isShuffle = false
    private var uri: Uri? = null
    private var playlistDetailAdapter: PlaylistDetailAdapter? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var musicService: MusicService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        mediaSessionCompat = MediaSessionCompat(baseContext, "My audio")
        getIntentMethod()
        var intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
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
        if(playlist_songs.isNotEmpty()) {
            intent = Intent(this, MusicService::class.java)
            intent.putExtra("servicePosition", position)
            startService(intent)
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
        if(musicService!=null) {
            if(musicService!!.isPlaying()){
                musicService!!.stop()
                musicService!!.release()
                uri = Uri.parse(playlist_songs[position].path)
                musicService!!.createMediaPlayer(position)
                setImage(uri.toString())
                playlist_playButton.setImageResource(R.drawable.stop)
                playlist_name_detail.text = playlist_songs[position].name
                musicService!!.start()
            }
            else{
                uri = Uri.parse(playlist_songs[position].path)
                musicService!!.createMediaPlayer(position)
                setImage(uri.toString())
                playlist_playButton.setImageResource(R.drawable.stop)
                playlist_name_detail.text = playlist_songs[position].name
                musicService!!.start()
            }
        }
    }
    private fun addSong(){
        addButton.setOnClickListener {
           showDialog()
        }
    }
    private fun playBtnClick(){
        playlist_playButton.setOnClickListener {
            if(musicService!=null) {
                if(musicService!!.mediaPlayer==null){

                    Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                }
                else playPause()
            }
        }
    }
    private fun autoNext(){
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    private fun prevBtnClick(){
        playlist_prevButton.setOnClickListener {
            if(musicService!=null) {
                if(musicService!!.mediaPlayer==null){
                    Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                }
                else playPrev()
            }
        }
    }
    private fun nextBtnClick(){
        playlist_nextButton.setOnClickListener {
            if(musicService!=null) {
                if(musicService!!.mediaPlayer==null){
                    Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                }
                else playNext()
            }
        }
    }
    private fun repeatBtnClick(){
        playlist_repeatButton.setOnClickListener {
           if(musicService!=null) {
               if(musicService!!.mediaPlayer==null){
                   Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
               }
               else playRepeat()
           }
        }
    }
    private fun shuffleBtnClick(){
        playlist_shuffleButton.setOnClickListener {
           if(musicService!=null) {
               if(musicService!!.mediaPlayer==null){
                   Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
               }
               else playShuffle()
           }
        }
    }
    override fun playNext(){
        Toast.makeText(this, "PlayNext", Toast.LENGTH_SHORT).show()
        if(!isRepeat){
            position = (position+1)% playlist_songs.size
            if(isShuffle){
                position = (position until playlist_songs.size).random()
            }
        }
        musicService!!.stop()
        musicService!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        playlist_name_detail.text = playlist_songs[position].name
        musicService!!.start()
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    override fun playPrev(){
        Toast.makeText(this, "PlayPrev", Toast.LENGTH_SHORT).show()
        if(!isRepeat){
            position = if(position>0)  position -1 else playlist_songs.size-1
            if(isShuffle){
                position = (0..position).random()
            }
        }
        musicService!!.stop()
        musicService!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        playlist_name_detail.text = playlist_songs[position].name
        musicService!!.start()
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
    override fun playPause(){
        Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show()
        if(musicService!=null) {
            if(musicService!=null&&musicService!!.isPlaying()){
                musicService!!.pause()
                playlist_playButton.setImageResource(R.drawable.play)
            }
            else{
                musicService!!.start()
                playlist_playButton.setImageResource(R.drawable.stop)
            }
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
            if(musicService==null||musicService!!.mediaPlayer==null){
                uri = Uri.parse(playlist_songs[0].path)
                playlist_name_detail.text = playlist_songs[0].name
                setImage(uri.toString())
                musicService!!.createMediaPlayer(position)
                playlist_playButton.setImageResource(R.drawable.stop)
                musicService!!.start()
            }
        }
    }
    fun showNotification(playPauseBtn: Int, playNoti: Int){
        var intent = Intent(this,PlayerActivity::class.java)
        var contentIntent = PendingIntent.getActivity(this, 0,intent, 0)

        var prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ApplicationClass.ACTION_PREVIOUS)
        var prevPending = PendingIntent.getBroadcast(this, 0,prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var pauseIntent = Intent(this, NotificationReceiver::class.java).setAction(ApplicationClass.ACTION_PLAY)
        var pausePending = PendingIntent.getBroadcast(this, 0,pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ApplicationClass.ACTION_NEXT)
        var nextPending = PendingIntent.getBroadcast(this, 0,nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var image = getAlbumArt(Uri.parse(playlist_songs[position].path).toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        var nBuilder = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
        if(bitmap!=null){
            nBuilder.setSmallIcon(playPauseBtn).setLargeIcon(bitmap)
        }
        else{
            var bitmapNone = BitmapFactory.decodeResource(resources, R.drawable.song_image)
            nBuilder.setSmallIcon(playPauseBtn).setLargeIcon(bitmapNone)
        }

        nBuilder.setContentTitle(playlist_songs.get(position).name).
        setContentText(playlist_songs.get(position).artist).
        setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat!!.sessionToken)).
        addAction(R.drawable.ic_prev, "Previous", prevPending).
        addAction(playNoti, "Pause", pausePending).
        addAction(R.drawable.ic_next, "Next", nextPending).
        setPriority(NotificationCompat.PRIORITY_HIGH).
        setAutoCancel(true)
        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, nBuilder.build())
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null;
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        var myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()
        musicService?.setOnPlaylistDetailClick(this)
        if(playlist_songs.isNotEmpty()){
            Log.d("AAAAA", playlist_songs[0].name)

            uri = Uri.parse(playlist_songs[0].path)
            if(musicService!=null){
                setImage(uri.toString())
                playlist_name_detail.text = playlist_songs[0].name
                musicService!!.stop()
                musicService!!.release()
                //mediaPlayer = MediaPlayer.create(applicationContext, uri)
                musicService!!.createMediaPlayer(position)
                musicService!!.start()
            }
        }
        Toast.makeText(this, "connected"+ musicService, Toast.LENGTH_LONG).show()
        autoNext()
    }

    override fun onPause() {
        super.onPause()
       unbindService(this);
    }
}