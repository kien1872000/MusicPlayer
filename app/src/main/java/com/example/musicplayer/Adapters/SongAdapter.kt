package com.example.musicplayer.Adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musicplayer.Models.Song
import com.example.musicplayer.R
import java.util.HashMap

class SongAdapter(var context: Context?, var resources : Int, var songs:ArrayList<Song>) : BaseAdapter(){
    class ViewHolder(row: View){
        var song_name_textview : TextView
        var song_image : ImageView
        init{
            song_name_textview = row.findViewById(R.id.song_name) as TextView
            song_image = row.findViewById(R.id.song_image) as ImageView
        }
    }
    override fun getCount(): Int {
        return this.songs.size
    }

    override fun getItem(position: Int): Any {
        return songs.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        var viewHolder : ViewHolder
        if(convertView==null){
            var layoutInflater : LayoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(resources, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        }else{
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }
        var song : Song = getItem(position) as Song
        viewHolder.song_name_textview.text = song.name
        var image = getAlbumArt(songs.get(position).path)
        if(image.filterNotNull().isEmpty()){
            Glide.with(context!!).asBitmap()
                    .load(R.drawable.song_image)
                    .into(viewHolder.song_image)
        }
        else{
            Glide.with(context!!).asBitmap()
                    .load(image)
                    .into(viewHolder.song_image)
        }

        return view as View
    }
    private fun getAlbumArt(uri: String): Array<ByteArray?> {
        var retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        var art = arrayOf(retriever.embeddedPicture)
        retriever.release()
        return art
    }
}