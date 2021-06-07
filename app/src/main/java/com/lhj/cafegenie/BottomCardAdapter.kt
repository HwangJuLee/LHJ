package com.lhj.cafegenie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class BottomCardAdapter(var items: ArrayList<Place> = arrayListOf()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var item = items

    private lateinit var item_click: Item_Click

    interface Item_Click {
        fun onItem_click(v: View?, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.bottom_card_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewholder : ViewHolder = holder as ViewHolder
        viewholder.rv_title.setText(item.get(position).place_name)
        viewholder.root.setOnClickListener { view: View? ->

            if (item_click != null) {
                item_click?.onItem_click(view, position)
            }

        }
    }

    override fun getItemCount(): Int = item.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val root = itemView.findViewById<ConstraintLayout>(R.id.root);
        val rv_title = itemView.findViewById<TextView>(R.id.cafe_name_tv)
    }

    fun setOnItemClickListener(listener: Item_Click) {
        this.item_click = listener
    }

}