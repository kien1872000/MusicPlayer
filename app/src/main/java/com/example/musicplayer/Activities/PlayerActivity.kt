package com.example.musicplayer.Activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.BLACK
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import com.example.musicplayer.ActionPlaying
import com.example.musicplayer.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PREVIOUS
import com.example.musicplayer.ApplicationClass.Companion.CHANNEL_ID_2
import com.example.musicplayer.Fragments.AlbumFragment
import com.example.musicplayer.Models.Song
import com.example.musicplayer.MusicService
import com.example.musicplayer.NotificationReceiver
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.fragment_song_playing.*
import kotlinx.android.synthetic.main.fragment_song_playing.elapsedTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.playBtn
import kotlinx.android.synthetic.main.fragment_song_playing.positionBar
import kotlinx.android.synthetic.main.fragment_song_playing.remainingTimeLabel
import kotlinx.android.synthetic.main.fragment_song_playing.volumeBar
import kotlinx.android.synthetic.main.song.*
import kotlinx.android.synthetic.main.song.song_image

class PlayerActivity : AppCompatActivity(), ServiceConnection, ActionPlaying {
    //private var mediaPlayer: MediaPlayer? = null
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
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
        unbindService(this)
    }
    private fun getIntentMethod(){
        if(!flag) {
            flag = true
            playButton.setImageResource(R.drawable.stop)
        }

        position = intent.getIntExtra("position", -1)
        category = intent.getStringExtra("category")
        if(category!=null&&category=="albumDetail"){
            Log.d("12123", "yes")
            musicItems = AlbumDetailActivity.songs
        }
        else{
            Log.d("4545", "yes")
            musicItems = MainActivity.song_list
        }
        Log.d("144444", position.toString()+"=="+musicItems.size.toString())
        if(position>=0) {
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
        intent = Intent(this, MusicService::class.java)
        intent.putExtra("servicePosition", position)
        startService(intent)
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
            musicService!!.pause()
            playButton.setImageResource(R.drawable.play)
            showNotification(R.drawable.play, R.drawable.ic_play)

        } else {
            // Start
            musicService!!.start()
            showNotification(R.drawable.stop, R.drawable.ic_pause)
            playButton.setImageResource(R.drawable.stop)
        }
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
                isRepeat = false;
            }
            else{
                repeatButton.setColorFilter(Color.BLUE)
                isRepeat = true;
            }
        }
    }
    private fun shuffleBtnClick(){
        shuffleButton.setOnClickListener{
            if(isShuffle){
                shuffleButton.setColorFilter(Color.BLACK)
                isShuffle = false;
            }
            else{
                shuffleButton.setColorFilter(Color.BLUE)
                isShuffle = true;
            }
        }
    }
    private fun autoNext(){
        musicService?.mediaPlayer?.setOnCompletionListener {
            playNext()
        }
    }
    override fun playPrev(){
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
        musicService?.mediaPlayer?.setOnCompletionListener {
            playPrev()
        }
    }
    override fun playNext(){
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
    private var musicService: MusicService? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null;
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        var myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()
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
        setAutoCancel(true).
        setOnlyAlertOnce(true)
        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, nBuilder.build())
    }
}