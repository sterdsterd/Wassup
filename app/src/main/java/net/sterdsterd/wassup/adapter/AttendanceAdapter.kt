package net.sterdsterd.wassup.adapter

import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_attendance.view.*
import kotlinx.android.synthetic.main.item_edit.view.*
import net.sterdsterd.wassup.Attendance
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.DetailActivity
import net.sterdsterd.wassup.activity.MainActivity
import java.util.*


class AttendanceAdapter(val activity: MainActivity, val date: String, val items : MutableList<Triple<String, String, String>>) : RecyclerView.Adapter<AttendanceAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val pref = activity.getSharedPreferences("User", Context.MODE_PRIVATE)
        val classStr = pref.getString("class", "Null")
        val cal = Calendar.getInstance()
        val nowDate = "${cal.get(Calendar.YEAR)}${cal.get(Calendar.MONTH) + 1}${cal.get(Calendar.DAY_OF_MONTH)}"
        val filteredList = mutableListOf<String>()
        val firestore = FirebaseFirestore.getInstance()
            .collection("class")
            .document(classStr)
            .collection(nowDate)
            .document(items[position].first)
            .collection("info")
            .document("filter")
        firestore.get().addOnCompleteListener { ta ->
            SharedData.studentList.forEach { md ->
                if (ta.result?.get(md.id).toString().toBoolean())
                    filteredList.add(md.id)
            }
        }
        holder.taskName.text = items[position].second
        holder.card.setOnClickListener { v ->
            val intent = Intent(v.context, DetailActivity::class.java)
            intent.putExtra("taskName", items[position].second)
            intent.putExtra("id", items[position].first)
            intent.putExtra("date", date)
            intent.putExtra("filtered", filteredList.toTypedArray())
            v.context.startActivity(intent)
        }
        holder.icon.setImageResource(activity.resources.getIdentifier("ic_${items[position].third}", "drawable", activity.packageName))
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)) {
        val taskName = itemView.taskName
        val card = itemView.card
        val icon = itemView.icon
    }
}