package com.example.musicplayer.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.WHITE
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnSongClick
import com.example.musicplayer.R
import io.gresse.hugo.vumeterlibrary.VuMeterView
import kotlinx.android.synthetic.main.song_playlist_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PlaylistDetailAdapter(var context: Context?, var songs: ArrayList<Song>, var onSongClick: OnSongClick?):
    RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder>(){
    var selectedPosition = 0
    private var isFirst = true
    var lastPosition = 0
    var isPause = false
     var i = 0
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)  {
        var song_name: TextView
        var song_image: ImageView
        var delete_btn: ImageView
        var wave_bar: VuMeterView
        init{
            song_name = view.findViewById(R.id.song_playlist_name) as TextView
            song_image = view.findViewById(R.id.song_playlist_image) as ImageView
            delete_btn = view. findViewById(R.id.song_playlist_delete_btn) as ImageView
            wave_bar = view.findViewById(R.id.song_playlist_vumeter) as VuMeterView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_playlist_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       lastPosition = position
       holder.delete_btn.setImageResource(R.drawable.trash)
       holder.song_name.text = songs[position].name
       holder.wave_bar.visibility = View.INVISIBLE
       holder.itemView.setBackgroundColor(Color.BLACK)
       holder.wave_bar.resume(true)
       var image = getSongArt(songs[position].path)
       var bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
       if(bitmap==null){
           holder.song_image.setImageResource(R.drawable.song_image)
       }
       else{
           holder.song_image.setImageBitmap(bitmap)
       }

        if(position == selectedPosition){
            holder.itemView.setBackgroundColor(Color.GRAY)
//            holder.itemView.isSelected = true
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            isFirst = false
            holder.wave_bar.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener{
            onSongClick!!.onClickItem(position)
        }

        if(isPause) {
            isPause = false
            holder.wave_bar.pause()
        }
        else {
            holder.wave_bar.resume(true)
        }

        holder.delete_btn.setOnTouchListener { v, event ->
            Log.d("mmm444", "yes $position")
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    holder.delete_btn.setColorFilter(Color.parseColor("#8097a3"))
                }
                MotionEvent.ACTION_UP -> {
                    holder.delete_btn.colorFilter = null
                    onSongClick!!.onDeleteItem(position)
                }

            }
            true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
    fun addList(songsAddList: ArrayList<Song>) {
        val start = songs.size
        songs.addAll(songsAddList)
        notifyDataSetChanged()
    }
    private fun getSongArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

}