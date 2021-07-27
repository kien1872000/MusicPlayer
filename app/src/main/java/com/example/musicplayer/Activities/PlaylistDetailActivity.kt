package com.example.musicplayer.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.musicplayer.*
import com.example.musicplayer.Adapters.PlaylistDetailAdapter
import com.example.musicplayer.Fragments.PlaylistFragment
import com.example.musicplayer.Fragments.SongSuggestFragment
import com.example.musicplayer.Models.Song
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.song.*
import kotlinx.android.synthetic.main.song_playlist_item.view.*

class PlaylistDetailActivity : AppCompatActivity(), OnSongClick, OnAcceptClickListener, ServiceConnection, OnPlaylistDetailClickListener {
    private var playlist_songs: ArrayList<Song> = ArrayList<Song>()
    private var position = 0
    private var anim: ObjectAnimator? = null
    private var playlistDetailAdapter: PlaylistDetailAdapter? = null
    private var play_list_name: String? = null
    private var flag = false
    private var isRepeat = false
    private var isShuffle = false
    private var uri: Uri? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var musicService: MusicService? = null
    private var isBound = false;
    private var playlistPosition = -1
    private var isFavorite = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        initActionButtonsView()
        setRotateSongImage()
        mediaSessionCompat = MediaSessionCompat(baseContext, "My audio")
        getIntentMethod()
        if(playlist_songs.isNotEmpty()) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            playlist_songs_listView!!.layoutManager = layoutManager
            playlistDetailAdapter = PlaylistDetailAdapter(this, playlist_songs, this)
            playlist_songs_listView!!.adapter = playlistDetailAdapter
            playlist_songs_listView!!.setHasFixedSize(true)
            anim!!.start()
            bindMusicService()
        }
    }

    private fun initActionButtonsView(){
        playlist_detail_backButton.setColorFilter(Color.WHITE)
        playlist_detail_favoriteButton.setColorFilter(Color.WHITE)
        playlist_shuffleButton.setColorFilter(Color.WHITE)
        playlist_repeatButton.setColorFilter(Color.WHITE)
        playlist_detail_addButton.setColorFilter(Color.WHITE)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun favoriteBtnClick() {
        playlist_detail_favoriteButton.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> playlist_detail_favoriteButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    if(!isFavorite) {
                        isFavorite = true
                        playlist_detail_favoriteButton!!.setColorFilter(Color.parseColor("#ff4081"))
                    }
                    else {
                        isFavorite = false
                        playlist_detail_favoriteButton!!.setColorFilter(Color.WHITE)
                    }
                }
            }
            true
        }

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
                playlist_songs.add(MainActivity.song_list[4])
                playlist_songs.add(MainActivity.song_list[5])
                playlist_songs.add(MainActivity.song_list[8])
                playlist_songs.add(MainActivity.song_list[9])

            }
            1 ->{
                playlist_songs.add(MainActivity.song_list[4])
                playlist_songs.add(MainActivity.song_list[15])

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
        val tempPosition = this.position
        this.position = position
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        if(musicService!=null) {
            playlistDetailAdapter!!.notifyItemChanged(tempPosition)
            playlistDetailAdapter!!.selectedPosition = position
            playlistDetailAdapter!!.notifyItemChanged(position)
            if(musicService!!.isPlaying()){
                musicService!!.stop()
                musicService!!.release()
                uri = Uri.parse(playlist_songs[position].path)
                playlist_artist_detail.text = playlist_songs[position].artist
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
                playlist_artist_detail.text = playlist_songs[position].artist
                musicService!!.start()
            }
            autoNext()
        }
    }

    override fun onDeleteItem(position: Int) {
       confirmDelete(position)
    }
    private fun deletePlaylistItem(position: Int) {
        val tempPosition = position
        Log.d("ggggf",
            position.toString()
        )
        playlist_songs.removeAt(position)
        playlist_songs_listView.adapter!!.notifyItemRemoved(position)
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
    @SuppressLint("ClickableViewAccessibility")
    private fun addSong(){
        playlist_detail_addButton.setOnTouchListener { v, event ->
             when(event.action){
                 MotionEvent.ACTION_DOWN -> playlist_detail_addButton.setColorFilter(Color.parseColor("#99ddff"))
                 MotionEvent.ACTION_UP -> {
                     playlist_detail_addButton.setColorFilter(Color.WHITE)
                     showDialog()
                 }
             }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun playBtnClick(){
        playlist_playButton.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> playlist_playButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    playlist_playButton.colorFilter = null
                    if(musicService!=null) {
                        if(musicService!!.mediaPlayer==null){

                            Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                        }
                        else playPause()
                    }
                }
            }
            true
        }
    }
    private fun autoNext(){
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun prevBtnClick(){
        playlist_prevButton.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> playlist_prevButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    playlist_prevButton.colorFilter = null
                    if(musicService!=null) {
                        if(musicService!!.mediaPlayer==null){

                            Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                        }
                        else playPrev()
                    }
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun nextBtnClick(){
        playlist_nextButton.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> playlist_nextButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    playlist_nextButton.colorFilter = null
                    if(musicService!=null) {
                        if(musicService!!.mediaPlayer==null){

                            Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                        }
                        else playNext()
                    }
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun repeatBtnClick(){
        playlist_repeatButton.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    playlist_repeatButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_UP -> {
                    if(musicService!=null) {
                        if(musicService!!.mediaPlayer==null){
                            Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                        }
                        else playRepeat()
                    }
                }
            }
            true
        }

    }
    @SuppressLint("ClickableViewAccessibility")
    private fun shuffleBtnClick(){
        playlist_shuffleButton.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> playlist_shuffleButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    if(musicService!=null) {
                        if(musicService!!.mediaPlayer==null){
                            Toast.makeText(this, "Không có bài hát nào được thêm", Toast.LENGTH_SHORT).show()
                        }
                        else playShuffle()
                    }

                }
            }
            true
        }

    }
    override fun playNext(){
//        Toast.makeText(this, "PlayNext Hế lồ", Toast.LENGTH_SHORT).show()
        playlistDetailAdapter!!.notifyItemChanged(position)
        musicService!!.reset()
        if(!isRepeat) {
            position = (position + 1) % playlist_songs.size
            if (isShuffle) {
                position = (position until playlist_songs.size).random()
            }
        }
       playlist_songs_listView.smoothScrollToPosition(position)
        playlistDetailAdapter!!.selectedPosition = position
        playlistDetailAdapter!!.notifyItemChanged(position)
//        stopWaveBar()
//
//
//        if(position<playlist_songs_listView.size) {
//            playlist_songs_listView[position].song_playlist_vumeter.resume(true)
//            playlist_songs_listView[position].song_playlist_vumeter.visibility = View.VISIBLE
//        }
        if(position>=musicService!!.musicFiles.size) passDataToMusicService()
        anim!!.start()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        playlist_name_detail.text = playlist_songs[position].name
        playlist_artist_detail.text = playlist_songs[position].artist
        musicService!!.start()
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist
        autoNext()
    }
    override fun playPrev(){
//        Toast.makeText(this, "PlayPrev Hế lồ", Toast.LENGTH_SHORT).show()
        playlistDetailAdapter!!.notifyItemChanged(position)
        if(!isRepeat){
            position = if(position>0)  position -1 else playlist_songs.size-1
            if(isShuffle){
                position = (0..position).random()
            }
        }
        anim!!.start()
        musicService!!.stop()
        musicService!!.release()
        uri = Uri.parse(playlist_songs[position].path)
        musicService!!.createMediaPlayer(position)
        setImage(uri.toString())
        playlist_playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        playlist_name_detail.text = playlist_songs[position].name
        playlist_artist_detail.text = playlist_songs[position].artist
        musicService!!.start()
        playlist_songs_listView.smoothScrollToPosition(position)
        playlistDetailAdapter!!.selectedPosition = position
        playlistDetailAdapter!!.notifyItemChanged(position)
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path;
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name;
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist;
        autoNext()
    }
    private fun playRepeat(){
        if(!isRepeat){
            isRepeat = true;
            playlist_repeatButton.setColorFilter(Color.parseColor("#E45D32D5"))
        }
        else{
            isRepeat = false
            playlist_repeatButton.setColorFilter(Color.WHITE)
        }

    }
    private fun playShuffle(){
        if(!isShuffle){
            isShuffle = true;
            playlist_shuffleButton.setColorFilter(Color.parseColor("#E45D32D5"))
        }
        else{
            isShuffle = false
            playlist_shuffleButton.setColorFilter(Color.WHITE)
        }

    }
    override fun playPause(){
//        Toast.makeText(this, "PlayPause hế lồ", Toast.LENGTH_SHORT).show()
        if(musicService!=null) {
            if(musicService!!.isPlaying()){
                anim!!.pause()
                musicService!!.pause()
                showNotification(R.drawable.play, R.drawable.ic_play)
                playlist_playButton.setImageResource(R.drawable.play)
                MiniPlayer.PLAY_PAUSE = "Pause"
                playlistDetailAdapter!!.isPause = true
                playlistDetailAdapter!!.notifyItemChanged(position)
            }
            else{
                anim!!.resume()
                musicService!!.start()
                showNotification(R.drawable.stop, R.drawable.ic_pause)
                playlist_playButton.setImageResource(R.drawable.stop)
                playlistDetailAdapter!!.notifyItemChanged(position)
                MiniPlayer.PLAY_PAUSE = "Play"
            }
        }
        MiniPlayer.PATH_TO_FRAG = playlist_songs[position].path;
        MiniPlayer.SONG_NAME_TO_FRAG = playlist_songs[position].name;
        MiniPlayer.SONG_ARTIST_TO_FRAG = playlist_songs[position].artist;

    }
    @SuppressLint("ClickableViewAccessibility")
    private fun backBtnClick(){
        playlist_detail_backButton?.setOnTouchListener{v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> playlist_detail_backButton.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    playlist_detail_backButton.setColorFilter(Color.WHITE)
                    onBackPressed()
                }
            }
            true
        }
    }
    override fun onResume() {
        super.onResume()
        favoriteBtnClick()
        shuffleBtnClick()
        repeatBtnClick()
        prevBtnClick()
        nextBtnClick()
        playBtnClick()
        backBtnClick()
        autoNext()
        Log.d("rrrr1", "Yes")
        addSong()
    }
    private fun showDialog(){
        val newFragment = SongSuggestFragment(this, playlist_songs)
        newFragment.show(supportFragmentManager, "add song dialog")
    }

    override fun onClick(songs: ArrayList<Song>) {
        val start = playlist_songs.size
        playlistDetailAdapter!!.songs.addAll(songs)
        playlist_songs_listView.setHasFixedSize(false)
        playlistDetailAdapter!!.notifyItemRangeInserted(start, songs.size)
        playlist_songs_listView.setHasFixedSize(true)


//        playlist_songs_listView.adapter!!.notifyItemInserted(lastSizeOfPlaylist)
//        playlist_songs_listView.adapter!!.notifyItemRangeInserted(lastSizeOfPlaylist, songs.size)
//        playlist_songs_listView.adapter!!.notifyDataSetChanged()
        //Log.d("3333", playlist_songs_listView!!.get(playlist_songs.size-1).song_playlist_name.text.toString())
        musicService!!.musicFiles = playlist_songs


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
        var intent = Intent(this,PlaylistDetailAdapter::class.java)
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
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putString(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYLIST_DETAIL_ACTIVITY)
        editor?.apply()
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
        startPlaylist()
        Toast.makeText(this, "connected"+ musicService, Toast.LENGTH_LONG).show()
        autoNext()

    }
    private fun startPlaylist() {
        if(playlist_songs.isNotEmpty()){
            if(!isBound) {
                showNotification(R.drawable.stop, R.drawable.ic_pause)
                uri = Uri.parse(playlist_songs[0].path)
                setImage(uri.toString())
                playlist_name_detail.text = playlist_songs[0].name
                playlist_artist_detail.text = playlist_songs[0].artist
                musicService!!.stop()
                musicService!!.release()
                playlistDetailAdapter!!.notifyItemChanged(0)
                //mediaPlayer = MediaPlayer.create(applicationContext, uri)
                musicService!!.createMediaPlayer(position)
                musicService!!.start()
                if(playlist_songs_listView!=null)
                isBound = true;
            }
        }
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
//        val intent = Intent(this,MainActivity::class.java)
//        startActivityForResult(intent, 1)
        if(isBound) {
            unbindService(this)
            isBound = false;
        }
    }
    private fun setRotateSongImage() {
        anim = ObjectAnimator.ofFloat(playlist_image_detail, "rotation", 0F, 360F)
        anim!!.duration = 7500
        anim!!.repeatCount = Animation.INFINITE
        anim!!.interpolator = LinearInterpolator()
        anim!!.repeatMode = ObjectAnimator.RESTART
    }




}