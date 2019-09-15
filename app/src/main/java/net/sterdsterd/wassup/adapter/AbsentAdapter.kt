package net.sterdsterd.wassup.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_absent.view.*
import kotlinx.android.synthetic.main.item_edit.view.*
import kotlinx.android.synthetic.main.item_edit.view.imgProfile
import kotlinx.android.synthetic.main.item_edit.view.list_item
import kotlinx.android.synthetic.main.item_edit.view.name
import kotlinx.android.synthetic.main.item_edit.view.phone
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.AbsentActivity
import net.sterdsterd.wassup.activity.InfoActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class AbsentAdapter(val activity: AbsentActivity, val items : MutableList<Pair<String, String>>) : RecyclerView.Adapter<AbsentAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val info = SharedData.studentList.first { it.id == items[position].first }
        holder.name.text = info.name
        holder.phone.text = PhoneNumberUtils.formatNumber(info.phone, Locale.getDefault().country)
        holder.item.setOnClickListener { v ->
            val intent = Intent(v.context, InfoActivity::class.java)
            intent.putExtra("id", info.id)
            v.context.startActivity(intent)
        }

        Log.d("dex", "${info.id}${info.hash}.jpg")
        val file = File(activity.applicationContext?.externalCacheDir.toString()).listFiles().filter { it.name == "${info.id}${info.hash}.jpg" }.firstOrNull()
        Glide.with(activity)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.imgProfile)

        holder.busIcon.visibility = if (items[position].second == "ABSENT") View.GONE else View.VISIBLE
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_absent, parent, false)) {
        val name = itemView.name
        val phone = itemView.phone
        val item = itemView.list_item
        val imgProfile = itemView.imgProfile
        val busIcon = itemView.busIcon
    }
}