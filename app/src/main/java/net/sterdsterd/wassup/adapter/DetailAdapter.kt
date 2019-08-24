package net.sterdsterd.wassup.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_detail.view.*
import net.sterdsterd.wassup.activity.InfoActivity
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.R
import net.sterdsterd.wassup.activity.DetailActivity
import java.io.File


class DetailAdapter(val activity: DetailActivity, val items : MutableList<MemberData>) : RecyclerView.Adapter<DetailAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)

    override fun getItemCount(): Int = items.size


    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.name.text = items[position].name
        holder.stat.text = "RSSI : ${items[position].rssi}"
        holder.card.setOnClickListener {
            val intent = Intent(it.context, InfoActivity::class.java)
            intent.putExtra("id", items[position].id)
            activity.startActivityForResult(intent, 2)

        }

        holder.iconBus.visibility = if (items[position].isBus) View.VISIBLE else View.INVISIBLE
        if (items[position].isDetected) holder.cardTint.background = ContextCompat.getDrawable(activity, R.drawable.gradient_card)
        else {
            holder.cardTint.background = ContextCompat.getDrawable(activity, R.drawable.gradient_card_warning)

            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("Notification", "${items[position].name}이 벗어났습니다")
            }

            val CHANNEL_ID = "childNotification"
            val CHANNEL_NAME = "아이 정보"
            val description = "아이의 정보를 알려드립니다."
            val importance = NotificationManager.IMPORTANCE_HIGH

            var notificationManager: NotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
                channel.description = description
                channel.enableLights(true)
                channel.lightColor = Color.CYAN
                channel.enableVibration(true)
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
            }

            var pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            var notificationBuilder = NotificationCompat.Builder(activity, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(activity.resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${items[position].name}이 벗어났습니다")
                .setContentText("ㅁㄴㅇㄹ")
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent)

            notificationManager.notify(0, notificationBuilder.build())
        }


        val file = File(activity.applicationContext?.externalCacheDir.toString()).listFiles().filter { it.name == "${items[position].id}${items[position].hash}.jpg" }[0]
        Glide.with(activity)
            .asBitmap()
            .load(file)
            .error(R.drawable.ic_profile)
            .into(holder.profile)

    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)) {
        val name = itemView.tvName
        val stat = itemView.tvStat
        val card = itemView.card
        val profile = itemView.profile
        val cardTint = itemView.cardTint
        val iconBus = itemView.iconBus
    }
}