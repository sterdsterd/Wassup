package net.sterdsterd.wassup.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_find.view.*
import net.sterdsterd.wassup.Activity.InfoActivity
import net.sterdsterd.wassup.Activity.MainActivity
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R


class FindAdapter(val activity: MainActivity, val items : MutableList<MemberData>) : RecyclerView.Adapter<FindAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.name.text = items[position].name
        holder.card.setOnClickListener { v ->
            val intent = Intent(v.context, InfoActivity::class.java)
            intent.putExtra("id", items[position].id)
            activity.startActivityForResult(intent, 2)
        }

    }


    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_find, parent, false)) {
        val name = itemView.tvName
        val stat = itemView.tvStat
        val card = itemView.card
    }
}