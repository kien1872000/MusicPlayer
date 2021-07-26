package com.example.musicplayer.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.Activities.PlaylistDetailActivity
import com.example.musicplayer.Models.Album
import com.example.musicplayer.R

class PlaylistAdapter(var context: Context?, var playlists: ArrayList<Album>) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    private val selectedPosition = 0
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playlist_name: TextView
        var playlist_image : ImageView
        init{
            playlist_name = view.findViewById(R.id.playlist_name) as TextView
            playlist_image = view.findViewById(R.id.playlist_image) as ImageView
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.playlist_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.playlist_name.text = playlists[position].name
//        var image = getAlbumArt(albums[position].path)
//        val bitmap = image?.size?.let { BitmapFactory.decodeByteArray(image, 0, it) }
        Glide.with(context!!).asBitmap()
            .load(R.drawable.song_image)
            .into(viewHolder.playlist_image)

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, PlaylistDetailActivity::class.java)
            intent.putExtra("playlistPosition", position)
            context?.startActivity(intent)
        }
        if(position==playlists.size-1) {
            viewHolder.playlist_name.text = "Thêm playlist ở đây"
            Glide.with(context!!).asBitmap()
                .load(R.drawable.add_image)
                .into(viewHolder.playlist_image)
        }
    }
    override fun getItemCount() = playlists.size

//    private fun getAlbumArt(uri: String): ByteArray? {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(uri)
//        val art = retriever.embeddedPicture
//        retriever.release()
//        return art
//    }

}