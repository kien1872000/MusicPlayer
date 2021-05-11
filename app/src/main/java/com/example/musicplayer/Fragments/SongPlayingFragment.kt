package com.example.musicplayer.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.fragment_song_playing.*


class SongPlayingFragment : Fragment(){
    private lateinit var mp: MediaPlayer
    private var totalTime: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_playing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mp = MediaPlayer.create(activity, R.raw.music)
        mp.isLooping = true
        mp.setVolume(0.5f, 0.5f)
        totalTime = mp.duration

        // Volume Bar
        volumeBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            var volumeNum = progress / 100.0f
                            mp.setVolume(volumeNum, volumeNum)
                        }
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }
                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                }
        )

        // Position Bar
        positionBar.max = totalTime
        positionBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mp.seekTo(progress)
                        }
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }
                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                }
        )

        // Thread
        Thread(Runnable {
            while (mp != null) {
                try {
                    var msg = Message()
                    msg.what = mp.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()
        playBtnClick()
    }
    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            // Update positionBar
            positionBar?.progress = currentPosition

            // Update Labels
            var elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabel?.text = elapsedTime

            var remainingTime = createTimeLabel(totalTime - currentPosition)
            remainingTimeLabel?.text = "-$remainingTime"
        }
    }
    fun createTimeLabel(time: Int): String {
        var timeLabel = ""
        var min = time / 1000 / 60
        var sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    fun playBtnClick() {
        playBtn.setOnClickListener {
            if (mp.isPlaying) {
                // Stop
                mp.pause()
                playBtn.setBackgroundResource(R.drawable.play)

            } else {
                // Start
                mp.start()
                playBtn.setBackgroundResource(R.drawable.stop)
            }
        }

    }

//    override fun onPause() {
//        super.onPause()
//        if(mp.isPlaying) mp.stop()
//        Log.d("Pause", "123")
//    }

}