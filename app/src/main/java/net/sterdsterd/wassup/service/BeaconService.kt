package net.sterdsterd.wassup.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.minew.beacon.*
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import net.sterdsterd.wassup.MemberData
import net.sterdsterd.wassup.SharedData
import net.sterdsterd.wassup.activity.MainActivity
import net.sterdsterd.wassup.fragment.MapFragment
import java.io.File
import java.util.*

class BeaconService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val mMinewBeaconManager = MinewBeaconManager.getInstance(this)
        try {
            mMinewBeaconManager.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //TODO : NEED TO WORK WHEN WIFI IS DISABLED
        var locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val student = mutableListOf<MemberData>()
        val tracking = mutableListOf<LatLng>()

        val firestore = FirebaseFirestore.getInstance()
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        firestore.collection("class").document(pref.getString("class", "Null")).collection("memberList").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { t ->
            if (t.isComplete) {
                val v = t.result?.documents?.size as Int
                for (i in 0 until v) {
                    student.add(
                        MemberData(
                            t.result?.documents?.get(i)?.id!!,
                            t.result?.documents?.get(i)?.getString("name")!!,
                            t.result?.documents?.get(i)?.getString("parentPhone")!!,
                            t.result?.documents?.get(i)?.getString("mac"),
                            t.result?.documents?.get(i)?.getString("hash")!!,
                            t.result?.documents?.get(i)?.getString("type")!!
                        )
                    )
                }
            }
        }
        mMinewBeaconManager.setDeviceManagerDelegateListener(object : MinewBeaconManagerListener {
            override fun onRangeBeacons(minewBeacons: List<MinewBeacon>) {

                Thread {
                    for (i in 0 until student.size) {
                        var rssiSeq = listOf<MinewBeacon>()
                        if(student.isNotEmpty()) rssiSeq = minewBeacons.filter { it.getBeaconValue(
                            BeaconValueIndex.MinewBeaconValueIndex_MAC).stringValue == student[i].mac }
                        if(rssiSeq.isNotEmpty()) {
                            student[i].rssi = rssiSeq[0].getBeaconValue(
                                BeaconValueIndex.MinewBeaconValueIndex_RSSI).intValue

                            if (ContextCompat.checkSelfPermission( this@BeaconService, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                                locationProviderClient.lastLocation.addOnSuccessListener {
                                    student[i].vec = Pair(LatLng(it.latitude, it.longitude), Calendar.getInstance().time)
                                }
                            }

                            student[i].isDetected = true
                            student[i].undetected = 0
                        } else {
                            student[i].undetected++
                            if (student[i].undetected > 5)
                                student[i].isDetected = false
                        }
                    }
                    if (ContextCompat.checkSelfPermission( this@BeaconService, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                        locationProviderClient.lastLocation.addOnSuccessListener {
                            tracking.add(LatLng(it.latitude, it.longitude))
                        }
                    }
                    SharedData.studentList = student
                    SharedData.tracking = tracking
                }.start()
            }

            override fun onAppearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onDisappearBeacons(minewBeacons: List<MinewBeacon>) { }

            override fun onUpdateState(state: BluetoothState) { }
        })
    }




    private fun sendNotification(title: String, body: String) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("Notification", body)
        }

        val CHANNEL_ID = "childNotification"
        val CHANNEL_NAME = "아이 정보"
        val description = "아이의 정보를 알려드립니다."
        val importance = NotificationManager.IMPORTANCE_HIGH

        var notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.CYAN
            channel.enableVibration(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.sym_def_app_icon))
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
