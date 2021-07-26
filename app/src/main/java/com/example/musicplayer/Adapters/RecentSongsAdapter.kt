package com.example.musicplayer.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.Activities.PlayerActivity
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R
import com.example.musicplayer.Util

class RecentSongsAdapter(var context: Context?, var songs: ArrayList<Song>): RecyclerView.Adapter<RecentSongsAdapter.ViewHolder>(){

    class  ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var songName: TextView = view.findViewById(R.id.song_name) as TextView
        var songImage : ImageView = view.findViewById(R.id.song_image) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.song_in_main_screen_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.songName.text = songs[position].name
        var image = Util.getAlbumArt(songs[position].path)
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap==null){
            Glide.with(context!!).asBitmap()
                .load(R.drawable.song_image)
                .into(holder.songImage)
        }
        else  holder.songImage.setImageBitmap(bitmap)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("category", "recent")
            context?.startActivity(intent)
        }
    }

}