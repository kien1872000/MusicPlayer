package com.example.musicplayer.Fragments

import android.content.*
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.MusicService
import com.example.musicplayer.R
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
class MiniPlayerFragment : Fragment(), ServiceConnection {
    private var musicService: MusicService? = null;
    private var isStart = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    mini_player_image.setImageResource(R.drawable.song_image)
                }
                mini_player_song_name.text = MiniPlayer.SONG_NAME_TO_FRAG
                mini_player_song_artist.text = MiniPlayer.SONG_ARTIST_TO_FRAG
                if(MiniPlayer.PLAY_PAUSE == "Play") {
                    Log.d("T1221", MiniPlayer.PATH_TO_FRAG.toString())
                    mini_player_play_btn.setImageResource(R.drawable.stop)
                }
                else {
                    mini_player_play_btn.setImageResource(R.drawable.play)
                }
            }
        }
        mini_player_next_btn?.setOnClickListener {
            Toast.makeText(activity, "Next", Toast.LENGTH_LONG).show()
            musicService?.clickNext()
            onMiniPlayerUpdate()
        }
        mini_player_play_btn?.setOnClickListener {
            Toast.makeText(activity, "Play", Toast.LENGTH_LONG).show()
            if(musicService!=null) {
                musicService!!.clickPlay()
                if(musicService!!.isPlaying()) {
                    mini_player_play_btn.setImageResource(R.drawable.stop)
                }
                else {
                    mini_player_play_btn.setImageResource(R.drawable.play)
                }
            }

        }
        mini_player_prev_btn?.setOnClickListener {
            Toast.makeText(activity, "Prev", Toast.LENGTH_LONG).show()
            musicService?.clickPrev()
            onMiniPlayerUpdate()
        }
    }
//    private fun onClickPlayBtn() {
//        if(musicService!!.isPlaying()) {
//            mini_player_play_btn.setImageResource(R.)
//        }
//    }

    private fun onMiniPlayerUpdate() {
        val image = getAlbumArt(MiniPlayer.PATH_TO_FRAG.toString())
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap!=null){
            mini_player_image.setImageBitmap(bitmap)
        }
        else{
            mini_player_image.setImageResource(R.drawable.song_image)
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
}