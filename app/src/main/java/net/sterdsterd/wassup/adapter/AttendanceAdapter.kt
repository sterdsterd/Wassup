package net.sterdsterd.wassup.adapter

import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_attendance.view.*
import kotlinx.android.synthetic.main.item_edit.view.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.activity.DetailActivity
import net.sterdsterd.wassup.activity.MainActivity
import java.util.*


class AttendanceAdapter(val activity: MainActivity, val date: String, val items : MutableList<Pair<String, String>>) : RecyclerView.Adapter<AttendanceAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.taskName.text = items[position].first
        holder.card.setOnClickListener { v ->
            val intent = Intent(v.context, DetailActivity::class.java)
            intent.putExtra("taskName", items[position])
            intent.putExtra("date", date)
            v.context.startActivity(intent)
        }
        holder.icon.setImageResource(activity.resources.getIdentifier("ic_${items[position].second}", "drawable", activity.packageName))
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)) {
        val taskName = itemView.taskName
        val card = itemView.card
        val icon = itemView.icon
    }
}