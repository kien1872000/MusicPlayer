package com.example.musicplayer.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Models.Category
import com.example.musicplayer.OnClickCategoryItemListener
import com.example.musicplayer.R

class CategoryAdapter(var context: Context?, var categories:ArrayList<Category>, var onClickCategoryItemListener: OnClickCategoryItemListener):
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var category_name_textview : TextView = view.findViewById(R.id.category_name) as TextView
        var category_image : ImageView = view.findViewById(R.id.category_image) as ImageView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.category_image.setImageResource(categories[position].image)
        holder.category_name_textview.text = categories[position].name
        holder.itemView.setOnClickListener{
            onClickCategoryItemListener?.onClickCategoryItem(position)
        }
    }

}