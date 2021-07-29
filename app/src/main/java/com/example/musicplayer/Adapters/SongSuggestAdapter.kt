package com.example.musicplayer.Adapters

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activities.PlaylistDetailActivity
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnSongClick
import com.example.musicplayer.R
import com.example.musicplayer.SongSuggestClickListener
import org.w3c.dom.Text

class SongSuggestAdapter(var context: Context?, var songs: ArrayList<Song>, var onItemClick: SongSuggestClickListener?, var type: Int =0):
    RecyclerView.Adapter<SongSuggestAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)  {
        var song_name: TextView
        var song_image: ImageView
        init{
            song_name = view.findViewById(R.id.song_name) as TextView
            song_image = view.findViewById(R.id.song_image) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_suggest_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.song_name.text = songs[position].name
        var image = getSongart(songs[position].path)
        var bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap==null){
            holder.song_image.setImageResource(R.drawable.song_image)
        }
        else{
            holder.song_image.setImageBitmap(bitmap)
        }
        holder.itemView.setOnClickListener{
            onItemClick!!.onClick(position)
        }
    }
    private fun getSongart(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

}