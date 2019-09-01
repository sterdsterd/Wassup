package net.sterdsterd.wassup.adapter

import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_icon.view.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.activity.DetailActivity
import net.sterdsterd.wassup.activity.IconActivity
import java.util.*


class IconAdapter(val activity: IconActivity, val items: MutableList<Int>) : RecyclerView.Adapter<IconAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount() = items.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.icon.setImageResource(items[position])

        holder.bg.setOnClickListener {
            activity.mIntent.putExtra("icon", items[position])
            activity.setResult(1, activity.mIntent)
            activity.finish()
        }

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false)) {
        val icon = itemView.imgIcon
        val bg = itemView.list_item
    }
}