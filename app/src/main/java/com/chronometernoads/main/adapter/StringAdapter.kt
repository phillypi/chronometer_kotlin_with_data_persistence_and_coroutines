package com.chronometernoads.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chronometernoads.R

class StringAdapter(var strings: List<String>) : RecyclerView.Adapter<StringAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fragment_chronometer_lap, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.timeHistory.text = strings[position]
        holder.numberOnList.text = (position+1).toString()+" - "
    }

    override fun getItemCount(): Int {
        return strings.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeHistory: TextView
        var numberOnList: TextView

        init {
            timeHistory = itemView.findViewById(R.id.item_fragment_chronometer_text_view)
            numberOnList = itemView.findViewById(R.id.item_fragment_chronometer_text_view_number)
        }
    }
}
