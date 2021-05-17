package com.example.musicplayer.Adapters

import android.R.attr.data
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Models.Song
import com.example.musicplayer.OnSongClick
import com.example.musicplayer.R


class PlaylistDetailAdapter(var context: Context?, var songs: ArrayList<Song>, var onSongClick: OnSongClick?):
    RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)  {
        var song_name: TextView
        var song_image: ImageView
        init{
            song_name = view.findViewById(R.id.song_playlist_name) as TextView
            song_image = view.findViewById(R.id.song_playlist_image) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_playlist_item, parent, false)
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
           onSongClick!!.onClickItem(position)
       }
    }
    fun update(datas: ArrayList<Song>) {
        songs.clear()
        songs.addAll(datas)
        notifyDataSetChanged()
    }
    private fun getSongart(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

}