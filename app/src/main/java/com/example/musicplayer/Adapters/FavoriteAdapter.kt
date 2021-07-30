package com.example.musicplayer.Adapters

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.example.musicplayer.Activities.PlayerActivity
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R
import maes.tech.intentanim.CustomIntent


class FavoriteAdapter(var context: Context?, var songs: ArrayList<Song>, var category_name: String) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var songName: TextView
        var songImage : ImageView
        var songArtists: TextView
        init{
            songName = view.findViewById(R.id.song_name) as TextView
            songImage = view.findViewById(R.id.song_image) as ImageView
            songArtists = view.findViewById(R.id.song_artists) as TextView
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.song, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.songName.text = songs[position].name
        viewHolder.songArtists.text = songs[position].artist
        var image = getAlbumArt(songs[position].path)
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap==null){
            Glide.with(context!!).asBitmap()
                .load(R.drawable.album_image)
                .into(viewHolder.songImage)
        }
        else  viewHolder.songImage.setImageBitmap(bitmap)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("category", "favorite")
            intent.putExtra("favoriteList", songs)
//            viewHolder.itemView.context.startActivity(intent,
//                ActivityOptions.makeCustomAnimation(viewHolder.itemView.context as Activity, R.anim.animate_spin_enter, R.anim.animate_spin_exit)
//                    .toBundle())
            viewHolder.itemView.context.startActivity(intent, ActivityOptions.makeBasic().toBundle())
        }
    }
    override fun getItemCount() = songs.size

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }
}

