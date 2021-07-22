package com.example.musicplayer.Activities

import android.R.attr.*
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.musicplayer.*
import com.example.musicplayer.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.example.musicplayer.ApplicationClass.Companion.CHANNEL_ID_2
import com.example.musicplayer.Models.Song
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.fragment_song_playing.elapsedTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.positionBar
import kotlinx.android.synthetic.main.fragment_song_playing.remainingTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.volumeBar
import kotlinx.android.synthetic.main.song.song_image


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
        mediaSessionCompat = MediaSessionCompat(baseContext, "My audio")
        getIntentMethod()
        onVolumeBarChange()
        onPositionBarChange()

//        this@PlayerActivity.runOnUiThread(
//            object : Runnable {
//                override fun run() {
//                    var currentPosition = musicService!!.getCurrentPosition()
//                    // Update positionBar
//                    positionBar?.progress = currentPosition
//                    // Update Labels
//                    var elapsedTime = createTimeLabel(currentPosition)
//                    elapsedTimeLabel?.text = elapsedTime
//
//                    var remainingTime = createTimeLabel(totalTime - currentPosition)
//                    remainingTimeLabel?.text = "-$remainingTime"
//                    handle.postDelayed(this, 1000)
//                }
//            }
//        )
    }
    private fun playThread(){
        var playThread = Thread{
            playBtnClick()
        }
        playThread.start()
    }
    override fun onResume() {
        var intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        shuffleBtnClick()
        prevBtnClick()
        autoNext()
        nextBtnClick()
        playBtnClick()
        repeatBtnClick()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putString(ServiceCommunication.SENDER_ACTIVITY, ServiceCommunication.PLAYER_ACTIVITY)
        editor?.apply()
        unbindService(this)
        stopRotateSongImage()
    }
    private fun getIntentMethod(){
        if(!flag) {
            flag = true
            playButton.setImageResource(R.drawable.stop)
        }

        position = intent.getIntExtra("position", -1)
        category = intent.getStringExtra("category")
        if(category!=null&&category=="albumDetail"){
            musicItems = AlbumDetailActivity.songs
        }
        else{
            musicItems = MainActivity.song_list
        }
        if(position>=0) {
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
//        else{
//            //mediaPlayer = MediaPlayer.create(applicationContext, uri)
//            musicService!!.createMediaPlayer(position)
//            musicService
//            musicService!!.start()
//        }
        showNotification(R.drawable.stop, R.drawable.ic_pause)
        passDataToMusicService()
       // mediaPlayer!!.isLooping = true
//        musicService!!.setVolume(0.5f, 0.5f)
//        totalTime = musicService!!.getDuration()
//        positionBar.max = totalTime
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
    private fun playBtnClick() {
        playButton.setOnClickListener {
            playPause()
        }
    }
    private fun prevBtnClick(){
        prevButton.setOnClickListener {
            playPrev()
        }
    }
    private fun nextBtnClick(){
        nextButton.setOnClickListener {
            playNext()
        }
    }
    private fun repeatBtnClick(){
        repeatButton.setOnClickListener{
            if(isRepeat){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    repeatButton.setColorFilter(Color.BLACK)
                }
                isRepeat = false
            }
            else{
                repeatButton.setColorFilter(Color.BLUE)
                isRepeat = true
            }
        }
    }
    private fun shuffleBtnClick(){
        shuffleButton.setOnClickListener{
            if(isShuffle){
                shuffleButton.setColorFilter(Color.BLACK)
                isShuffle = false
            }
            else{
                shuffleButton.setColorFilter(Color.BLUE)
                isShuffle = true
            }
        }
    }
    private fun autoNext(){
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    override fun playPrev(){
        anim!!.resume()
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
        uri = Uri.parse(musicItems[position].path)
        val image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            song_image.setImageBitmap(bitmap)
        }
        else{
            song_image.setBackgroundResource(R.drawable.song_image)
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
    }
    override fun playNext(){
        anim!!.resume()
        musicService!!.reset()
        if(!isRepeat){
            position = (position+1)%musicItems.size
            if(isShuffle){
                position = (position until musicItems.size).random()
            }
        }
        uri = Uri.parse(musicItems[position].path)
        val image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            song_image.setImageBitmap(bitmap)
        }
        else{
            song_image.setImageResource(R.drawable.song_image)
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
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
        MiniPlayer.PATH_TO_FRAG = musicItems[position].path
        MiniPlayer.SONG_NAME_TO_FRAG = musicItems[position].name
        MiniPlayer.SONG_ARTIST_TO_FRAG = musicItems[position].artist
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
        Toast.makeText(this, "connected"+ musicService, Toast.LENGTH_LONG).show()
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
    fun showNotification(playPauseBtn: Int, playNoti: Int){
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
            var bitmapNone = BitmapFactory.decodeResource(resources, R.drawable.song_image)
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
        Log.d("BBBaA", "yes sir ${song_image.pivotX}")
        song_image.startAnimation(rotateAnimation)
    }
    private fun stopRotateSongImage() {
        anim = ObjectAnimator.ofFloat(song_image, "rotation", 0F, 360F)
        anim!!.duration = 7500
        anim!!.repeatCount = Animation.INFINITE
        anim!!.interpolator = LinearInterpolator()
       anim!!.repeatMode = ObjectAnimator.RESTART
    }
}