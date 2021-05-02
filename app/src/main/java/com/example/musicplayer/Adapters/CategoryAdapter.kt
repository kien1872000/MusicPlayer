package com.example.musicplayer.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.musicplayer.Models.Category
import com.example.musicplayer.R

class CategoryAdapter(var context: Context?, var resources : Int, var categories:ArrayList<Category>) : BaseAdapter(){
    class ViewHolder(row: View){
        var category_name_textview : TextView
        var category_image : ImageView
        init{
            category_name_textview = row.findViewById(R.id.category_name) as TextView
            category_image = row.findViewById(R.id.category_image) as ImageView
        }
    }
    override fun getCount(): Int {
        return this.categories.size
    }

    override fun getItem(position: Int): Any {
        return categories.get(position)
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
        var category : Category  = getItem(position) as Category
        viewHolder.category_name_textview.text = category.name
        viewHolder.category_image.setImageResource(category.image)
        return view as View
    }

}