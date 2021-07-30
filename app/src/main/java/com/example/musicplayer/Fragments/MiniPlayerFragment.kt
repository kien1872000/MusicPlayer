package com.example.musicplayer.Fragments

import android.animation.ObjectAnimator
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicplayer.*
import com.example.musicplayer.Activities.PlayerActivity
import kotlinx.android.synthetic.main.fragment_mini_player.*
import kotlinx.android.synthetic.main.song.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MiniPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MiniPlayerFragment : Fragment(), ServiceConnection, OnMiniPlayerChangeListener{
    private var musicService: MusicService? = null;
    private var anim: ObjectAnimator? =null
    private var isStart = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stopRotateSongImage()
        anim!!.start()
//        Log.d("T111", MiniPlayer.PATH_TO_FRAG.toString())
//        var preferences: SharedPreferences = activity!!.
//        getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
//        val path = preferences.getString(MiniPlayer.MUSIC_FILE, null)
//        val songName = preferences.getString(MiniPlayer.SONG_NAME, null)
//        val songArtist = preferences.getString(MiniPlayer.SONG_ARTIST, null)
//        if(path!=null) {
//            MiniPlayer.IS_SHOW_MINI_PLAYER = true
//            MiniPlayer.PATH_TO_FRAG = path
//            MiniPlayer.SONG_ARTIST_TO_FRAG =  songArtist
//            MiniPlayer.SONG_NAME_TO_FRAG = songName
//            MiniPlayer.SONG_ARTIST_TO_FRAG = songArtist
//        }
//        else{
//            MiniPlayer.IS_SHOW_MINI_PLAYER = false;
//            MiniPlayer.PATH_TO_FRAG = null;
//            MiniPlayer.SONG_ARTIST_TO_FRAG = null
//            MiniPlayer.SONG_NAME_TO_FRAG = null
//        }

    }
    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }

    override fun onResume() {
        super.onResume()
        var preferences: SharedPreferences = activity!!.
        getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
        val path = preferences.getString(MiniPlayer.MUSIC_FILE, null)
        val songName = preferences.getString(MiniPlayer.SONG_NAME, null)
        val songArtist = preferences.getString(MiniPlayer.SONG_ARTIST, null)
        if(path!=null) {
            MiniPlayer.IS_SHOW_MINI_PLAYER = true
            MiniPlayer.PATH_TO_FRAG = path
            MiniPlayer.SONG_ARTIST_TO_FRAG =  songArtist
            MiniPlayer.SONG_NAME_TO_FRAG = songName
            MiniPlayer.SONG_ARTIST_TO_FRAG = songArtist
        }
        else{
            MiniPlayer.IS_SHOW_MINI_PLAYER = false;
            MiniPlayer.PATH_TO_FRAG = null;
            MiniPlayer.SONG_ARTIST_TO_FRAG = null
            MiniPlayer.SONG_NAME_TO_FRAG = null
        }
        var intent = Intent(activity, MusicService::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
//        if(musicService!=null&&musicService!!.mediaPlayer!=null) {
//            if(musicService!!.isPlaying()) {
//
//                mini_player_play_btn.setImageResource(R.drawable.stop)
//            }
//            else {
//                mini_player_play_btn.setImageResource(R.drawable.play)
//            }
//        }
        if(MiniPlayer.IS_SHOW_MINI_PLAYER) {
            if(MiniPlayer.PATH_TO_FRAG!=null) {
                val image = getAlbumArt(MiniPlayer.PATH_TO_FRAG.toString())
                val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
                if(bitmap!=null){
                    mini_player_image.setImageBitmap(bitmap)
                }
                else{
                    mini_player_image.setImageResource(R.drawable.album_image)
                }
                mini_player_song_name.text = MiniPlayer.SONG_NAME_TO_FRAG
                mini_player_song_artist.text = MiniPlayer.SONG_ARTIST_TO_FRAG
                if(MiniPlayer.PLAY_PAUSE == "Play") {
                    anim!!.resume()
                    mini_player_play_btn.setImageResource(R.drawable.stop)
                }
                else {
                    anim!!.pause()
                    mini_player_play_btn.setImageResource(R.drawable.play)
                }
            }
        }
        mini_player_next_btn?.setOnClickListener {

            playNextSelf()
        }
        mini_player_play_btn?.setOnClickListener {
           playPauseSelf()

        }
        mini_player_prev_btn?.setOnClickListener {
            playPrevSelf()
        }
//        onClickBottomBar()
    }
//    private fun onClickPlayBtn() {
//        if(musicService!!.isPlaying()) {
//            mini_player_play_btn.setImageResource(R.)
//        }
//    }
    private fun onMiniPlayerUpdate() {
        anim!!.start()
        val image = getAlbumArt(MiniPlayer.PATH_TO_FRAG.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            mini_player_image.setImageBitmap(bitmap)
        }
        else{
            mini_player_image.setImageResource(R.drawable.album_image)
        }
        mini_player_song_name.text = MiniPlayer.SONG_NAME_TO_FRAG
        mini_player_song_artist.text = MiniPlayer.SONG_ARTIST_TO_FRAG
        mini_player_play_btn.setImageResource(R.drawable.stop)
    }
    override fun onPause() {
        super.onPause()
        activity?.unbindService(this)

    }
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null;
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        var binder = service as MusicService.MyBinder;

        musicService = binder.getService()
        musicService!!.setOtherCallBack(this)
       // musicService!!.setCallBack(this)
//        musicService!!.createMediaPlayerWithPath(MiniPlayer.PATH_TO_FRAG.toString())
//        mini_player_play_btn.setImageResource(R.drawable.play)
//        mini_player_play_btn.setOnClickListener {
//            if(musicService!!.isPlaying()) {
//                musicService!!.stop()
//                mini_player_play_btn.setImageResource(R.drawable.play)
//            }
//            else{
//                musicService!!.start()
//                mini_player_play_btn.setImageResource(R.drawable.stop)
//            }
//        }

    }

    override fun playNext() {
//        Toast.makeText(activity, "Next2222", Toast.LENGTH_LONG).show()
        //musicService?.clickNext()
        onMiniPlayerUpdate()
    }

    override fun playPrev() {
//        Toast.makeText(activity, "Prev2222", Toast.LENGTH_LONG).show()
        onMiniPlayerUpdate()
    }

    override fun playPause() {
//        Toast.makeText(activity, "Play2222", Toast.LENGTH_LONG).show()
        if(musicService!=null) {
            //musicService!!.clickPlay()
            if(musicService!!.isPlaying()) {
                anim!!.resume()
                mini_player_play_btn.setImageResource(R.drawable.stop)
            }
            else {
                anim!!.pause()
                mini_player_play_btn.setImageResource(R.drawable.play)
            }
        }
    }
    private fun playNextSelf(){
        Toast.makeText(activity, "Next1111", Toast.LENGTH_LONG).show()
        musicService?.clickNext()
        onMiniPlayerUpdate()
    }
    private fun playPrevSelf(){
        Toast.makeText(activity, "Prev1111", Toast.LENGTH_LONG).show()
        musicService?.clickPrev()
        onMiniPlayerUpdate()
    }
    private fun onClickBottomBar(){
        mini_player_bar.setOnClickListener {
            val i = Intent(context, PlayerActivity::class.java)
            i.flags = FLAG_ACTIVITY_REORDER_TO_FRONT;
            startActivity(i)
        }
    }
    private fun playPauseSelf() {
        if(musicService!=null) {
            Toast.makeText(activity, "Pause1111", Toast.LENGTH_LONG).show()
            musicService!!.clickPlay()
            if(musicService!!.isPlaying()) {
                anim!!.resume()
                mini_player_play_btn.setImageResource(R.drawable.stop)
            }
            else {
                anim!!.pause()
                mini_player_play_btn.setImageResource(R.drawable.play)
            }
        }
    }
    private fun stopRotateSongImage() {
        anim = ObjectAnimator.ofFloat(mini_player_image, "rotation", 0F, 360F)
        anim!!.duration = 7500
        anim!!.repeatCount = Animation.INFINITE
        anim!!.interpolator = LinearInterpolator()
        anim!!.repeatMode = ObjectAnimator.RESTART
    }
}