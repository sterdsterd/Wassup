package net.sterdsterd.wassup.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_edit.view.*
import net.sterdsterd.wassup.activity.EditActivity
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R


class EditAdapter(val activity: MainActivity, val items : MutableList<MemberData>) : RecyclerView.Adapter<EditAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.name.text = items[position].name
        holder.item.setOnClickListener { v ->
            val intent = Intent(v.context, EditActivity::class.java)
            intent.putExtra("id", items[position].id)
            activity.startActivityForResult(intent, 1)
            //v.context.startActivity(intent)
        }

        val storage = FirebaseStorage.getInstance().reference
        storage.child("profile/${items[position].id}.png").downloadUrl.addOnSuccessListener {
            Glide.with(activity.applicationContext).load(it).apply(RequestOptions.circleCropTransform().override(300)).into(holder.imgProfile)
        }

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_edit, parent, false)) {
        val name = itemView.name
        val item = itemView.list_item
        val imgProfile = itemView.imgProfile
    }
}