package net.sterdsterd.wassup.Adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_edit.view.*
import net.sterdsterd.wassup.Activity.EditActivity
import net.sterdsterd.wassup.Activity.MainActivity
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

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_edit, parent, false)) {
        val name = itemView.name
        val item = itemView.list_item
    }
}