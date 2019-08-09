package net.sterdsterd.wassup.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
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
        holder.stat.text = items[position].rssi.toString() //"${Math.pow(10.0, (-59.0 - items[position].rssi) / (10 * 2))}m"
        holder.card.setOnClickListener {
            val intent = Intent(it.context, InfoActivity::class.java)
            intent.putExtra("id", items[position].id)
            activity.startActivityForResult(intent, 2)

        }

        val storage = FirebaseStorage.getInstance().reference
        storage.child("profile/${items[position].id}.png").downloadUrl.addOnSuccessListener {
            Glide.with(activity).load(it).apply(RequestOptions.circleCropTransform().override(300)).into(holder.profile)
        }
    }


    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_find, parent, false)) {
        val name = itemView.tvName
        val stat = itemView.tvStat
        val card = itemView.card
        val profile = itemView.profile
    }
}