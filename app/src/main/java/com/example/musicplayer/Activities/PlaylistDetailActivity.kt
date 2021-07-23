package com.example.musicplayer.Activities

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
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
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import com.example.musicplayer.Adapters.PlaylistDetailAdapter
import com.example.musicplayer.Fragments.PlaylistFragment
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
import kotlinx.android.synthetic.main.song_playlist_item.view.*
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
    private var isBound = false;
    private var playlistPosition = -1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        mediaSessionCompat = MediaSessionCompat(baseContext, "My audio")
        getIntentMethod()
        if(playlist_songs.isNotEmpty()) {
            bindMusicService()
        }
//        if(playlist_songs.size>=1){
        playlistDetailAdapter = PlaylistDetailAdapter(this, playlist_songs, this)
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        playlist_songs_listView?.layoutManager = layoutManager
        playlist_songs_listView?.adapter = playlistDetailAdapter
        playlist_songs_listView?.setHasFixedSize(true)
//       }
    }
    private fun getIntentMethod(){
        if(!flag){
            flag = true
            playlist_playButton.setImageResource(R.drawable.stop)
        }
        playlistPosition = intent.getIntExtra("playlistPosition", -1);
        initPlaylist()


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
        when(playlistPosition){
            0 -> {
                playlist_songs.add(MainActivity.song_list[0])
                playlist_songs.add(MainActivity.song_list[1])
                playlist_songs.add(MainActivity.song_list[2])
                playlist_songs.add(MainActivity.song_list[3])
            }
            1 ->{
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
        stopWaveBar()
        playlist_songs_listView[position].song_playlist_vumeter.resume(true)
        playlist_songs_listView[position].song_playlist_vumeter.visibility = View.VISIBLE
        showNotification(R.drawable.stop, R.drawable.ic_pause)
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

    override fun onDeleteItem(position: Int) {
       confirmDelete(position)
    }
    private fun deletePlaylistItem(position: Int) {
        playlist_songs.removeAt(position)
        musicService!!.musicFiles = playlist_songs;
        if(position<this.position) this.position--
        else if(position==this.position) {
            this.position--
            if(playlist_songs.size>0) playNext()
            else {
                deletePlaylist()
                var onPlaylistItemChangeListener: OnPlaylistItemChangeListener = PlaylistFragment()
                onPlaylistItemChangeListener.onDeletePlaylistItem(playlistPosition)
            }

        }
        musicService!!.position = this.position
        playlistDetailAdapter!!.notifyDataSetChanged()
    }
    private fun deletePlaylist() {
        onBackPressed()
        //unbindService(this)
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putBoolean(MiniPlayer.START_PLAYER_ACTIVITY, false)
        editor?.apply();
        val notifyManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.cancelAll()
        var intent = Intent(this, MusicService::class.java)
        stopService(intent)
    }
    private fun confirmDelete(position: Int) {
        var msg: String = "Bạn chắc chắn muốn xóa bài hát này?"
        if(playlist_songs.size<=1) {
            msg = "Bạn có chắc muốn xóa bài hát này? Nếu xóa thì playlist này sẽ bị xóa luôn";
        }
        val builder: AlertDialog.Builder? = this?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(msg)?.setTitle("Xác nhận")
        builder?.setPositiveButton("Có", DialogInterface.OnClickListener {
                dialog, id->
            deletePlaylistItem(position)

        })
        builder?.setNegativeButton("Không", DialogInterface.OnClickListener {
                dialog, id->

        })
        builder?.show()
    }
    private fun addSong(){
        addButton.setOnClickListener {
           showDialog()
//            if(musicService!=null&&mus)
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
        Toast.makeText(this, "PlayNext Hế lồ", Toast.LENGTH_SHORT).show()
        if(!isRepeat){
            position = (position+1)% playlist_songs.size
            if(isShuffle){
                position = (position until playlist_songs.size).random()
            }
        }
        Log.d("BBBaA", "yes sir $position")
        stopWaveBar()
        if(position<playlist_songs_listView.size) {
            playlist_songs_listView[position].song_playlist_vumeter.resume(true)
            playlist_songs_listView[position].song_playlist_vumeter.visibility = View.VISIBLE
        }
//        if(position>=musicService!!.musicFiles.size) passDataToMusicService()
        musicService!!.stop()
        musicService!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        playlist_name_detail.text = playlist_songs[position].name
        musicService!!.start()
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path;
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name;
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist;
    }
    private fun stopWaveBar() {
        for(view in playlist_songs_listView) {
            view.song_playlist_vumeter.visibility = View.INVISIBLE
        }
    }
    override fun playPrev(){
        Toast.makeText(this, "PlayPrev Hế lồ", Toast.LENGTH_SHORT).show()
        if(!isRepeat){
            position = if(position>0)  position -1 else playlist_songs.size-1
            if(isShuffle){
                position = (0..position).random()
            }
        }
        stopWaveBar()
        if(position<playlist_songs_listView.size) {
            playlist_songs_listView[position].song_playlist_vumeter.resume(true)
            playlist_songs_listView[position].song_playlist_vumeter.visibility = View.VISIBLE
        }
        musicService!!.stop()
        musicService!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        playlist_name_detail.text = playlist_songs[position].name
        musicService!!.start()
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path;
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name;
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist;
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
        Toast.makeText(this, "PlayPause hế lồ", Toast.LENGTH_SHORT).show()
        if(musicService!=null) {
            if(musicService!!.isPlaying()){
                playlist_songs_listView[position].song_playlist_vumeter.pause()
                musicService!!.pause()
                showNotification(R.drawable.play, R.drawable.ic_play)
                playlist_playButton.setImageResource(R.drawable.play)
                MiniPlayer.PLAY_PAUSE = "Pause"
            }
            else{
                playlist_songs_listView[position].song_playlist_vumeter.resume(true)
                musicService!!.start()
                showNotification(R.drawable.stop, R.drawable.ic_pause)
                playlist_playButton.setImageResource(R.drawable.stop)
                MiniPlayer.PLAY_PAUSE = "Play"
            }
        }
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path;
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name;
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist;

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
        val newFragment = SongSuggestFragment(this, playlist_songs)
        newFragment.show(supportFragmentManager, "add song dialog")
    }

    override fun onClick(songs: ArrayList<Song>) {
        playlist_songs.addAll(songs);
        playlistDetailAdapter!!.notifyDataSetChanged()
        musicService?.musicFiles = playlist_songs
        if(musicService==null) bindMusicService()
//        if(playlist_songs.isNotEmpty()){
//
//            if(!isBound){
//                bindMusicService()
//
////                  uri = Uri.parse(playlist_songs[0].path)
////                  playlist_name_detail.text = playlist_songs[0].name
////                  setImage(uri.toString())
////                  musicService!!.createMediaPlayer(position)
////                  playlist_playButton.setImageResource(R.drawable.stop)
////                  musicService!!.start()
//            }
//            else {
//                Log.d("AAAAA", "yes sir ${playlist_songs.size}")
//                passDataToMusicService()
//            }
//        }
    }
    private fun showNotification(playPauseBtn: Int, playNoti: Int){
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
        musicService?.actionPlaying =null;
        musicService?.setOnPlaylistDetailClick(this)
        if(playlist_songs.isNotEmpty()){
          if(!isBound) {
              playlist_songs_listView[0].song_playlist_vumeter.visibility = View.VISIBLE
              showNotification(R.drawable.stop, R.drawable.ic_pause)
              uri = Uri.parse(playlist_songs[0].path)
              setImage(uri.toString())
              playlist_name_detail.text = playlist_songs[0].name
              musicService!!.stop()
              musicService!!.release()
              //mediaPlayer = MediaPlayer.create(applicationContext, uri)
              musicService!!.createMediaPlayer(position)
              musicService!!.start()
              isBound = true;
          }
        }
        Toast.makeText(this, "connected"+ musicService, Toast.LENGTH_LONG).show()
        autoNext()

    }
    private fun passDataToMusicService() {
        intent = Intent(this, MusicService::class.java)
        intent.putExtra(ServiceCommunication.SERVICE_POSITION, position)
        intent.putExtra(ServiceCommunication.GET_SONGS_LIST_ACTION, playlist_songs)
        intent.putExtra(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY)
        startService(intent)
    }
    private fun bindMusicService() {
        intent = Intent(this, MusicService::class.java)
        intent.putExtra(ServiceCommunication.SERVICE_POSITION, this.position)
        intent.putExtra(ServiceCommunication.GET_SONGS_LIST_ACTION, playlist_songs)
        intent.putExtra(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY)
        startService(intent)
        var intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }
    override fun onPause() {
        super.onPause()
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putString(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY)
        editor?.apply()
        if(isBound) {
            unbindService(this)
            isBound = false;
        }
    }
}