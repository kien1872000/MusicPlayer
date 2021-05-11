package com.example.musicplayer.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.BLACK
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import androidx.core.os.postDelayed
import com.example.musicplayer.Fragments.AlbumFragment
import com.example.musicplayer.Models.Song
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

class PlayerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var uri: Uri? = null
    private var totalTime: Int = 0
    private var position: Int = -1
    private var flag: Boolean = false
    private var handle: Handler = Handler()
    private var isRepeat  = false
    private var isShuffle = false
    private var musicItems= ArrayList<Song>()
    private var category: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        getIntentMethod()
        onVolumeBarChange()
        onPositionBarChange()
        this@PlayerActivity.runOnUiThread(
            object : Runnable {
                override fun run() {
                    var currentPosition = mediaPlayer!!.currentPosition
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
    }
    override fun onResume() {
        shuffleBtnClick()
        prevBtnClick()
        autoNext()
        nextBtnClick()
        playBtnClick()
        repeatBtnClick()
        super.onResume()
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
        uri = Uri.parse(musicItems[position].path)
        var image = getAlbumArt(uri.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            song_image.setImageBitmap(bitmap)
        }
        song_name_text.text = musicItems[position].name
        singer_name_text.text = musicItems[position].artist
        if(mediaPlayer!=null){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            mediaPlayer!!.start()
        }
        else{
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            mediaPlayer!!.start()
        }
       // mediaPlayer!!.isLooping = true
        mediaPlayer!!.setVolume(0.5f, 0.5f)
        totalTime = mediaPlayer!!.duration
        positionBar.max = totalTime
    }
    private fun onVolumeBarChange(){
        volumeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        var volumeNum = progress / 100.0f
                        mediaPlayer!!.setVolume(volumeNum, volumeNum)
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
                        mediaPlayer!!.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {
                }
                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )
    }
    private fun playBtnClick() {
        playButton.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                // Stop
                mediaPlayer!!.pause()
                playButton.setImageResource(R.drawable.play)

            } else {
                // Start
                mediaPlayer!!.start()
                playButton.setImageResource(R.drawable.stop)
            }
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
                repeatButton.setColorFilter(Color.BLACK)
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
        mediaPlayer!!.setOnCompletionListener {
            playNext()
        }
    }
    private fun playPrev(){
        mediaPlayer!!.reset()
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
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        mediaPlayer!!.setVolume(volumeBar.progress/100f, volumeBar.progress/100f)
        mediaPlayer!!.start()
        totalTime = mediaPlayer!!.duration
        positionBar.max = totalTime
        playButton.setImageResource(R.drawable.stop)
        mediaPlayer?.setOnCompletionListener {
            playPrev()
        }
    }
    private fun playNext(){
        mediaPlayer!!.reset()
        if(!isRepeat){
            position = (position+1)%musicItems.size
            if(isShuffle){
                position = (position until musicItems.size-1).random()
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
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        mediaPlayer!!.setVolume(volumeBar.progress/100f, volumeBar.progress/100f)
        mediaPlayer!!.start()
        totalTime = mediaPlayer!!.duration
        positionBar.max = totalTime
        playButton.setImageResource(R.drawable.stop)
        mediaPlayer?.setOnCompletionListener {
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
}