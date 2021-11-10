package com.lhj.cafegenie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class BottomCardAdapter(var items: ArrayList<CafeData.Place> = arrayListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var item = items
    private lateinit var itemClick: ItemClick
    private lateinit var favoriteClick: ItemClick

    interface ItemClick {
        fun onItemClick(v: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.bottom_card_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewholder : ViewHolder = holder as ViewHolder
        viewholder.cafeName.text = item.get(position).place_name
        viewholder.root.setOnClickListener { view: View? ->

            if (itemClick != null) {
                itemClick?.onItemClick(view, position)
            }

        }
        viewholder.favorite_iv.setOnClickListener { view: View? ->
            if (favoriteClick != null) {
                favoriteClick?.onItemClick(view, position)
            }
        }
    }

    override fun getItemCount(): Int = item.size

    fun setData(cafeData: ArrayList<CafeData.Place>){
        item = cafeData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val root : ConstraintLayout = itemView.findViewById(R.id.root);
        val cafeName: TextView = itemView.findViewById(R.id.cafe_name_tv)
        val favorite_iv: ImageView = itemView.findViewById(R.id.favorite_iv)
    }

    fun setOnItemClickListener(listener: ItemClick) {
        this.itemClick = listener
    }

    fun setOnFavoriteClickListener(listener: ItemClick) {
        this.favoriteClick = listener
    }

}