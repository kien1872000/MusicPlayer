package com.example.musicplayer.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.Activities.AlbumDetailActivity
import com.example.musicplayer.Activities.PlayerActivity
import com.example.musicplayer.Models.Album
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R

class AlbumAdapter(var context: Context?, var albums: ArrayList<Album>) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var album_name: TextView
        var album_image : ImageView
        init{
            album_name = view.findViewById(R.id.album_name) as TextView
            album_image = view.findViewById(R.id.album_image) as ImageView
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.album, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.album_name.text = albums[position].name
        var image = getAlbumArt(albums[position].path)
        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        if(bitmap==null){
            Glide.with(context!!).asBitmap()
                .load(R.drawable.album_image)
                .into(viewHolder.album_image)
        }
        else  viewHolder.album_image.setImageBitmap(bitmap)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, AlbumDetailActivity::class.java)
            intent.putExtra("position", position)
            context?.startActivity(intent)
        }
    }
    override fun getItemCount() = albums.size

    private fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release()
        return art
    }

}