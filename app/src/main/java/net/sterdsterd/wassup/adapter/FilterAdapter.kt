package net.sterdsterd.wassup.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_filter.view.*
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.AddActivity
import net.sterdsterd.wassup.activity.FilterActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class FilterAdapter(val activity: FilterActivity, val items : MutableList<MemberData>) : RecyclerView.Adapter<FilterAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.chkbox.isChecked = items[position].isChecked
        holder.name.text = items[position].name
        Log.d("dex", "${items[position].id}${items[position].hash}.jpg")
        val file = File(activity.applicationContext?.externalCacheDir.toString()).listFiles().filter { it.name == "${items[position].id}${items[position].hash}.jpg" }.firstOrNull()
        Glide.with(activity)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.imgProfile)

        holder.item.setOnClickListener {
            holder.chkbox.isChecked = !holder.chkbox.isChecked
            items[position].isChecked = holder.chkbox.isChecked
            SharedData.studentList.forEach {
                Log.d("dext", "${it.name} -> ${it.isChecked}")
            }
            Log.d("dext", "-----------------------------")
        }

        holder.chkbox.setOnClickListener {
            items[position].isChecked = holder.chkbox.isChecked
            SharedData.studentList.forEach {
                Log.d("dext", "${it.name} -> ${it.isChecked}")
            }
        }

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)) {
        val name = itemView.name
        val imgProfile = itemView.imgProfile
        val item = itemView.list_item
        val chkbox = itemView.chkbox
    }
}