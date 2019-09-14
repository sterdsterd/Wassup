package net.sterdsterd.wassup.adapter

import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import net.sterdsterd.wassup.service.BeaconService
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
        val isCheckedList = mutableListOf<String>()
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("class").document(classStr).collection(date).document(items[position].first)
            .collection("info").document("filter").get().addOnCompleteListener { ta ->
            SharedData.studentList.forEach { md ->
                if (ta.result?.get(md.id).toString().toBoolean())
                    filteredList.add(md.id)
            }
            Log.d("dext-fil", filteredList.toString())
        }
        firestore.collection("class").document(classStr).collection(date).document(items[position].first).get().addOnCompleteListener { ta ->
            SharedData.studentList.forEach { md ->
                if (ta.result?.get(md.id).toString() != "null") {
                    isCheckedList.add(md.id)
                    Log.d("dext-chk", "${md.id} -> ${ta.result?.get(md.id)}")
                }
            }
            Log.d("dext-chk", isCheckedList.toString())
            holder.badgeText.text = isCheckedList.size.toString()
            holder.badgeBg.visibility = if (isCheckedList.size == 0) View.GONE
                                        else View.VISIBLE
        }
        holder.taskName.text = items[position].second
        holder.card.setOnClickListener { v ->
            //activity.stopService(Intent(activity, BeaconService::class.java))
            val intent = Intent(v.context, DetailActivity::class.java)
            intent.putExtra("taskName", items[position].second)
            intent.putExtra("id", items[position].first)
            intent.putExtra("icon", items[position].third)
            intent.putExtra("date", date)
            intent.putExtra("filtered", filteredList.toTypedArray())
            intent.putExtra("checked", isCheckedList.toTypedArray())
            activity.startActivityForResult(intent, 2)
        }
        holder.icon.setImageResource(activity.resources.getIdentifier("ic_${items[position].third}", "drawable", activity.packageName))
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)) {
        val taskName = itemView.taskName
        val card = itemView.card
        val icon = itemView.icon
        val badgeBg = itemView.badgeBg
        val badgeText = itemView.badgeText
    }
}