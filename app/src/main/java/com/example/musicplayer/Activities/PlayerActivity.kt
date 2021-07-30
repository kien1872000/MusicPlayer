package com.example.musicplayer.Activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.musicplayer.*
import com.example.musicplayer.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.example.musicplayer.ApplicationClass.Companion.CHANNEL_ID_2
import com.example.musicplayer.Fragments.MainScreenFragment
import com.example.musicplayer.Models.Song
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_playlist_detail.*
import kotlinx.android.synthetic.main.fragment_song_playing.elapsedTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.positionBar
import kotlinx.android.synthetic.main.fragment_song_playing.remainingTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.volumeBar
import kotlinx.android.synthetic.main.song.song_image
import maes.tech.intentanim.CustomIntent


class PlayerActivity : AppCompatActivity(), ServiceConnection, ActionPlaying {
    //private var mediaPlayer: MediaPlayer? = null
    private var anim: ObjectAnimator? =null
    private var uri: Uri? = null
    private var totalTime: Int = 0
    private var position: Int = -1
    private var flag: Boolean = false
    private var handle: Handler = Handler()
    private var isRepeat  = false
    private var isShuffle = false
    private var musicItems= ArrayList<Song>()
    private var category: String? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var musicService: MusicService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        stopRotateSongImage()
        repeatButton.setColorFilter(Color.WHITE)
        shuffleButton.setColorFilter(Color.WHITE)
        backButton.setColorFilter(Color.WHITE)
        mediaSessionCompat = MediaSessionCompat(baseContext, "My audio")
        getIntentMethod()
        onVolumeBarChange()
        onPositionBarChange()
    }

    override fun onResume() {
        var intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        backBtnClick()
        shuffleBtnClick()
        repeatBtnClick()
        autoNext()
        prevBtnClick()
        nextBtnClick()
        playBtnClick()
        favoriteBtnClick()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("VNNN", position.toString())
        MainActivity.musicPlayerDbHelper!!.setAllFavoritesAndHearTimes(musicItems)
        unbindService(this)

    }
    @SuppressLint("ClickableViewAccessibility")
    private fun backBtnClick(){
        backButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    backButton.setColorFilter(Color.parseColor("#a0acb2"))
                }
                MotionEvent.ACTION_CANCEL -> {
                    backButton.setColorFilter(Color.WHITE)
                    this.onBackPressed()
                }
                MotionEvent.ACTION_UP -> {
                    backButton.setColorFilter(Color.WHITE)
                    this.onBackPressed()

                }
            }
            true
        }
    }
    private fun getIntentMethod(){
        if(!flag) {
            flag = true
            playButton.setImageResource(R.drawable.stop)
        }

        position = intent.getIntExtra("position", -1)
        category = intent.getStringExtra("category")
        if(category!=null) {
            if(category=="albumDetail"){
                musicItems = AlbumDetailActivity.songs
            }
            else if(category=="recent"){
                musicItems = intent.getSerializableExtra("recentList") as ArrayList<Song>
            }
            else if(category=="listenALot"){
                musicItems = intent.getSerializableExtra("listALotList") as ArrayList<Song>
            }
            else if(category=="favorite"){
                musicItems = intent.getSerializableExtra("favoriteList") as ArrayList<Song>
            }
            else {
                musicItems = MainActivity.song_list
            }
        }

        if(position>=0) {
            musicItems[position].heardTimes++
            initFavoriteButtonView()
            anim!!.start()
            uri = Uri.parse(musicItems[position].path)
            var image = getAlbumArt(uri.toString())
            val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
            if(bitmap!=null){
                song_image.setImageBitmap(bitmap)
            }
            song_name_text.text = musicItems[position].name
            singer_name_text.text = musicItems[position].artist
            if(musicService!=null){
                musicService!!.stop()
                musicService!!.release()
                //mediaPlayer = MediaPlayer.create(applicationContext, uri)
                musicService!!.createMediaPlayer(position)
                musicService!!.start()
            }
        }
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        passDataToMusicService()
    }
    private fun onVolumeBarChange(){
        volumeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        var volumeNum = progress / 100.0f
                        musicService!!.setVolume(volumeNum, volumeNum)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )
    }
    private fun onPositionBarChange(){
        positionBar.max = totalTime
        positionBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        musicService!!.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )
    }

    override fun playPause() {
        if (musicService!!.isPlaying()) {
            // Stop
            anim!!.pause()
            musicService!!.pause()
            playButton.setImageResource(R.drawable.play)
            showNotification(R.drawable.play, R.drawable.ic_play)
            MiniPlayer.PLAY_PAUSE = "Pause"
        } else {
            // Start
            anim!!.resume()
            musicService!!.start()
            showNotification(R.drawable.stop, R.drawable.ic_pause)
            playButton.setImageResource(R.drawable.stop)
            MiniPlayer.PLAY_PAUSE = "Play"
        }
        MiniPlayer.PATH_TO_FRAG = musicItems[position].path
        MiniPlayer.SONG_NAME_TO_FRAG = musicItems[position].name
        MiniPlayer.SONG_ARTIST_TO_FRAG = musicItems[position].artist
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun playBtnClick() {
        playButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    playButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_CANCEL -> {
                    playPause()
                    playButton.colorFilter = null
                }
                MotionEvent.ACTION_UP -> {
                    playPause()
                    playButton.colorFilter = null
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun prevBtnClick(){
        prevButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    prevButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_UP -> {
                    playPrev()
                    prevButton.colorFilter = null
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun nextBtnClick(){
        nextButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    nextButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_CANCEL -> {
                    playNext()
                    nextButton.colorFilter = null
                }
                MotionEvent.ACTION_UP -> {
                    playNext()
                    nextButton.colorFilter = null
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun repeatBtnClick(){
        repeatButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    repeatButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_CANCEL -> {
                    playPrev()
                    if(isRepeat){
                        repeatButton.setColorFilter(Color.WHITE)
                        isRepeat = false
                    }
                    else{
                        repeatButton.setColorFilter(Color.parseColor("#E45D32D5"))
                        isRepeat = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    playPrev()
                    if(isRepeat){

                        repeatButton.setColorFilter(Color.WHITE)
                        isRepeat = false
                    }
                    else{
                        repeatButton.setColorFilter(Color.parseColor("#E45D32D5"))
                        isRepeat = true
                    }
                }
            }
            true
        }
        repeatButton.setOnClickListener{
            if(isRepeat){

                repeatButton.setColorFilter(Color.WHITE)
                isRepeat = false
            }
            else{
                repeatButton.setColorFilter(Color.parseColor("#E45D32D5"))
                isRepeat = true
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun shuffleBtnClick(){
        shuffleButton.setOnTouchListener { arg0, arg1 ->
            when (arg1.action) {
                MotionEvent.ACTION_DOWN -> {
                    shuffleButton.setColorFilter(Color.parseColor("#99ddff"))
                }
                MotionEvent.ACTION_CANCEL -> {
                    if(isShuffle){
                        shuffleButton.setColorFilter(Color.WHITE)
                        isShuffle = false
                    }
                    else{
                        shuffleButton.setColorFilter(Color.parseColor("#E45D32D5"))
                        isShuffle = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if(isShuffle){
                        shuffleButton.setColorFilter(Color.WHITE)
                        isShuffle = false
                    }
                    else{
                        shuffleButton.setColorFilter(Color.parseColor("#E45D32D5"))
                        isShuffle = true
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
    override fun playPrev(){
        anim!!.start()
        musicService!!.reset()
        if(!isRepeat){
            if(position>0) position--
            else{
                position = musicItems.size-1
            }
            if(isShuffle){
                position = (0..position).random()
            }
        }
        musicItems[position].heardTimes++
        initFavoriteButtonView()
        uri = Uri.parse(musicItems[position].path)
        val image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            song_image.setImageBitmap(bitmap)
        }
        else{
            song_image.setImageResource(R.drawable.album_image)
        }
        song_name_text.text = musicItems[position].name
        singer_name_text.text = musicItems[position].artist
        //mediaPlayer = MediaPlayer.create(applicationContext, uri)
        musicService!!.createMediaPlayer(position)
        musicService!!.setVolume(volumeBar.progress/100f, volumeBar.progress/100f)
        musicService!!.start()
        totalTime = musicService!!.getDuration()
        positionBar.max = totalTime
        playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
//        musicService?.mediaPlayer?.setOnCompletionListener {
//            playPrev()
//        }
        MiniPlayer.PATH_TO_FRAG = musicItems[position].path
        MiniPlayer.SONG_NAME_TO_FRAG = musicItems[position].name
        MiniPlayer.SONG_ARTIST_TO_FRAG = musicItems[position].artist
        autoNext()
    }
    override fun playNext(){
        anim!!.start()
        musicService!!.reset()
        if(!isRepeat){
            position = (position+1)%musicItems.size
            if(isShuffle){
                position = (position until musicItems.size).random()
            }
        }
        musicItems[position].heardTimes++
        initFavoriteButtonView()
        uri = Uri.parse(musicItems[position].path)
        val image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            song_image.setImageBitmap(bitmap)
        }
        else{
            song_image.setImageResource(R.drawable.album_image)
        }
        song_name_text.text = musicItems[position].name
        singer_name_text.text = musicItems[position].artist
        //mediaPlayer = MediaPlayer.create(applicationContext, uri)
        musicService!!.createMediaPlayer(position)
        musicService!!.setVolume(volumeBar.progress/100f, volumeBar.progress/100f)
        musicService!!.start()
        totalTime = musicService!!.getDuration()
        positionBar.max = totalTime
        playButton.setImageResource(R.drawable.stop)
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        MiniPlayer.PATH_TO_FRAG = musicItems[position].path
        MiniPlayer.SONG_NAME_TO_FRAG = musicItems[position].name
        MiniPlayer.SONG_ARTIST_TO_FRAG = musicItems[position].artist
        autoNext()
    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
    private fun createTimeLabel(time: Int): String {
        var timeLabel = ""
        var min = time / 1000 / 60
        var sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }


    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
        //topService();

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        var myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()
        musicService?.onPlaylistDetailClickListener = null
        musicService?.setCallBack(this)
        musicService!!.setVolume(0.5f, 0.5f)
        totalTime = musicService!!.getDuration()
        positionBar.max = totalTime
        this@PlayerActivity.runOnUiThread(
            object : Runnable {
                override fun run() {
                    var currentPosition = musicService!!.getCurrentPosition()
                    // Update positionBar
                    positionBar?.progress = currentPosition
                    // Update Labels
                    var elapsedTime = createTimeLabel(currentPosition)
                    elapsedTimeLabel?.text = elapsedTime

                    var remainingTime = createTimeLabel(totalTime - currentPosition)
                    remainingTimeLabel?.text = "-$remainingTime"
                    handle.postDelayed(this, 1000)
                }
            }
        )
        autoNext()
    }
    private fun showNotification(playPauseBtn: Int, playNoti: Int){
        var intent = Intent(this,PlayerActivity::class.java)
        var contentIntent = PendingIntent.getActivity(this, 0,intent, 0)

        var prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREVIOUS)
        var prevPending = PendingIntent.getBroadcast(this, 0,prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var pauseIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY)
        var pausePending = PendingIntent.getBroadcast(this, 0,pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)
        var nextPending = PendingIntent.getBroadcast(this, 0,nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var image = getAlbumArt(Uri.parse(musicItems[position].path).toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        var nBuilder = NotificationCompat.Builder(this, CHANNEL_ID_2)
        if(bitmap!=null){
            nBuilder.setSmallIcon(playPauseBtn).setLargeIcon(bitmap)
        }
        else{
            var bitmapNone = BitmapFactory.decodeResource(resources, R.drawable.album_image)
            nBuilder.setSmallIcon(playPauseBtn).setLargeIcon(bitmapNone)
        }

        nBuilder.setContentTitle(musicItems.get(position).name).
        setContentText(musicItems.get(position).artist).
        setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat!!.sessionToken)).
        addAction(R.drawable.ic_prev, "Previous", prevPending).
        addAction(playNoti, "Pause", pausePending).
        addAction(R.drawable.ic_next, "Next", nextPending).
        setPriority(NotificationCompat.PRIORITY_HIGH).
        setAutoCancel(true)
        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, nBuilder.build())
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putString(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYER_ACTIVITY)
        editor?.apply()
    }
    private fun passDataToMusicService() {
        intent = Intent(this, MusicService::class.java)
        intent.putExtra(ServiceCommunication.SERVICE_POSITION, position)
        intent.putExtra(ServiceCommunication.GET_SONGS_LIST_ACTION,
            musicItems
        )
        intent.putExtra(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYER_ACTIVITY)
        startService(intent)
    }
    private fun rotateSongImage(){
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.duration = 7500
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.repeatMode
        song_image.startAnimation(rotateAnimation)
    }
    private fun stopRotateSongImage() {
        anim = ObjectAnimator.ofFloat(song_image, "rotation", 0F, 360F)
        anim!!.duration = 7500
        anim!!.repeatCount = Animation.INFINITE
        anim!!.interpolator = LinearInterpolator()
       anim!!.repeatMode = ObjectAnimator.RESTART
    }
    private fun initFavoriteButtonView() {
        if(musicItems[position].isFavorite==1) {
            favorite_button.setColorFilter(Color.parseColor("#ff4081"))
        }
        else {
            favorite_button.setColorFilter(Color.WHITE)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun favoriteBtnClick() {
        favorite_button.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> favorite_button.setColorFilter(Color.parseColor("#99ddff"))
                MotionEvent.ACTION_UP -> {
                    if(musicItems[position].isFavorite==0) {
                        musicItems[position].isFavorite = 1
                        favorite_button!!.setColorFilter(Color.parseColor("#ff4081"))
                    }
                    else {
                        musicItems[position].isFavorite = 0
                        favorite_button!!.setColorFilter(Color.WHITE)
                    }
                }
            }
            true
        }

    }

}