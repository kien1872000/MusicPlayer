package com.example.musicplayer.Activities

import com.example.musicplayer.MusicPlayerDbHelper
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicplayer.Fragments.MainScreenFragment
import com.example.musicplayer.MiniPlayer
import com.example.musicplayer.Models.Song
import com.example.musicplayer.MusicService
import com.example.musicplayer.R


class MainActivity : AppCompatActivity() {
    companion object {
        var musicPlayerDbHelper: MusicPlayerDbHelper? = null
        var song_list: ArrayList<Song> = ArrayList()
        const val REQUEST_CODE:Int=1;
        fun getAllMusic(context: Context): ArrayList<Song>{
            var tempMusicList = ArrayList<Song>()
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            var projection = arrayOf(
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
            )
            var cusor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            if(cusor!=null){
                while(cusor.moveToNext()){
                    var album: String = cusor.getString(0)
                    var title: String = cusor.getString(1)
                    var artists: String = cusor.getString(2)
                    var duration: String = cusor.getString(3)
                    var path: String = cusor.getString(4)
                    var song: Song = Song(-1, title, album, path, artists, 0, 0)
                    tempMusicList.add(song)
                }
                cusor.close()
            }
            else{
            //    Log.d("AAAA", "YES'")
            }
            return tempMusicList
        }
    }
    private var mainScreenFragment : MainScreenFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        musicPlayerDbHelper = MusicPlayerDbHelper(this)
        permisson()


    }
    private fun permisson(){
        if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
        else{
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            song_list = getAllMusic(this)
            if(musicPlayerDbHelper!!.getAllSong().isNullOrEmpty()) {
                musicPlayerDbHelper!!.insertSongs(song_list)
            }
            song_list = musicPlayerDbHelper!!.getAllSong()
            initView()
        }
    }
    private fun initView(){
        mainScreenFragment = MainScreenFragment()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.animate_shrink_enter, R.anim.animate_shrink_exit)
            .replace(R.id.main_id,mainScreenFragment!!)
            .commit();
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== REQUEST_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                song_list = getAllMusic(this)
                initView()
            }
            else{
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        var editor: SharedPreferences.Editor? = getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE).edit()
        editor?.putBoolean(MiniPlayer.START_PLAYER_ACTIVITY, false)
        editor?.apply();
        val notifyManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.cancelAll()
        var intent = Intent(this, MusicService::class.java)
//        musicPlayerDbHelper!!.close()
        stopService(intent)
    }





}